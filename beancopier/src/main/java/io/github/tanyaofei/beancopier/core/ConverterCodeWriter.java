package io.github.tanyaofei.beancopier.core;

import io.github.tanyaofei.beancopier.constants.*;
import io.github.tanyaofei.beancopier.converter.Converter;
import io.github.tanyaofei.beancopier.core.instancer.AllArgsTargetInstancer;
import io.github.tanyaofei.beancopier.core.instancer.NoArgsTargetInstancer;
import io.github.tanyaofei.beancopier.core.local.LocalDefiner;
import io.github.tanyaofei.beancopier.core.local.LocalDefiners;
import io.github.tanyaofei.beancopier.core.local.LocalDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalsDefinitionContext;
import io.github.tanyaofei.beancopier.utils.ClassSignature;
import io.github.tanyaofei.beancopier.utils.reflection.Reflections;
import io.github.tanyaofei.beancopier.utils.reflection.member.BeanMember;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 转换器字节码编写工具
 *
 * @since 0.1.5
 */
public class ConverterCodeWriter implements Opcodes, Methods {

  private final static LocalDefiner definer = LocalDefiners.getDefiner();
  private final ConverterDefinition definition;

  public ConverterCodeWriter(ConverterDefinition definition) {
    this.definition = definition;
  }

  /**
   * 编写无参构造函数
   *
   * @param cw ClassWriter
   */
  private void genConstructor(ClassWriter cw) {
    var v = cw.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, MethodNames.Object$init, MethodDescriptors.Object$init, null, null);
    v.visitCode();
    v.visitVarInsn(ALOAD, 0);
    v.visitMethodInsn(INVOKESPECIAL, InternalNames.Object, MethodNames.Object$init, MethodDescriptors.Object$init, false);
    v.visitInsn(RETURN);
    v.visitMaxs(-1, -1);
    v.visitEnd();
  }

  /**
   * 编写 Converter 字节码
   *
   * @return Converter 字节码字节数组
   */
  public byte[] write() {
    var sourceType = definition.getSourceType();
    var tc = definition.getTargetType();
    var cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    cw.visit(
        V1_8,
        ACC_PUBLIC | ACC_SYNTHETIC,
        definition.getInternalName(),
        ClassSignature.getClassSignature(
            ClassInfos.Object,
            ClassSignature.ClassInfo.of(Converter.class, sourceType, tc)
        ),
        InternalNames.Object,
        InternalNames.AConverter
    );
    cw.visitSource("<generated>", null);
    genConstructor(cw);
    genConvertMethod(cw);
    genLambda$convert$0Method(cw);
    if (sourceType != Object.class || tc != Object.class) {
      genConvertBridgeMethod(cw);
    }

    return cw.toByteArray();
  }

  /**
   * 编写如果为 null 直接返回 null 的代码片段
   * <pre>{@code
   * if (source == nul) {
   *   return null;
   * }
   * }</pre>
   *
   * @param v 方法编写器
   */
  private void returnNullIfNull(MethodVisitor v) {
    var ifnonnull = new Label();

    v.visitVarInsn(ALOAD, 1);
    v.visitJumpInsn(IFNONNULL, ifnonnull);
    v.visitInsn(ACONST_NULL);
    v.visitInsn(ARETURN);
    v.visitLabel(ifnonnull);
  }

  /**
   * 编写转换方法 {@link Converter#convert(Object)}
   * <pre>{@code
   * var var2 = source.getVar2();
   * var var3 = source.getVar3();
   * }</pre>
   *
   * <ul>
   *   <li>如果 target 为 {@link Record} 类，那么接下来会写入</li>
   *   <pre>{@code
   *   var target = new Target(var2, var3);
   *   }
   *   </pre>
   *   <li>否则接下来将写入</li>
   *   <pre>{@code
   *   var target = new Target();
   *   target.setVar2(var2);
   *   target.setVar3(var3);
   *   }</pre>
   * </ul>
   * <br>
   * 最后写入返回 target
   * <pre>{@code
   * return target;
   * }</pre>
   *
   * @param cw ClassWriter
   */
  private void genConvertMethod(ClassWriter cw) {
    var configuration = definition.getConfiguration();
    var v = cw.visitMethod(
        ACC_PUBLIC,
        MethodNames.Converter$convert,
        definition.getConvertMethodDescriptor(),
        null,
        null
    );
    v.visitCode();

    returnNullIfNull(v);

    var targetMembers = Reflections.getMembersWithSetter(definition.getTargetType(), configuration.isIncludingSuper());
    var sourceMembers = BeanMember.mapIterable(Reflections.getMembersWithGetter(definition.getSourceType(), configuration.isIncludingSuper()));

    int firstLocalStore = 2;  // 0: this, 1: source object ref
    var context = new LocalsDefinitionContext()
        .setSourceMembers(sourceMembers)
        .setNextStore(firstLocalStore);

    for (var tm : targetMembers) {
      var property = configuration.isPropertySupported()
          ? Properties.getOrDefault(tm)
          : Properties.defaultProperty;
      definer.define(
          v,
          definition,
          LocalDefinition
              .builder()
              // use filed name if it is cloning other while use value() of @Property and fallback to field name if it's empty
              .name(definition.isClone() ? tm.getName() : property.value().isEmpty() ? tm.getName() : property.value())
              .type(tm.getType())
              .genericType(tm.getGenericType())
              .skip(property.skip())
              .build(),
          context
      );
    }

    int targetStore = context.getNextStore();
    var instancer = switch (definition.getNewInstanceMode()) {
      case ALL_ARGS_CONSTRUCTOR -> new AllArgsTargetInstancer(
          v,
          definition,
          targetStore,
          targetMembers,
          firstLocalStore
      );
      case NO_ARGS_CONSTRUCTOR_THEN_GET_SET -> new NoArgsTargetInstancer(
          v,
          definition,
          targetStore,
          targetMembers,
          StreamSupport
              .stream(targetMembers.spliterator(), true)
              .filter(m -> (configuration.isPropertySupported() ? Properties.getOrDefault(m) : Properties.defaultProperty).skip())
              .collect(Collectors.toSet()),
          firstLocalStore
      );
    };
    instancer.newInstance();

    v.visitVarInsn(ALOAD, targetStore);
    v.visitInsn(ARETURN);
    v.visitMaxs(-1, -1);
    v.visitEnd();
  }

  /**
   * 编写 convert 的桥接方法
   * <pre>{@code
   * public Object convert(Object source) {
   *   return convert((Source) source);
   * }
   * }</pre>
   *
   * @param cw 类编写器
   */
  private void genConvertBridgeMethod(
      ClassWriter cw
  ) {
    var v = cw.visitMethod(
        ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC,
        MethodNames.Converter$convert,
        MethodDescriptors.Converter$convert,
        null,
        null);
    v.visitCode();
    v.visitVarInsn(ALOAD, 0);
    v.visitVarInsn(ALOAD, 1);
    v.visitTypeInsn(CHECKCAST, org.objectweb.asm.Type.getInternalName(definition.getSourceType()));
    v.visitMethodInsn(
        INVOKEVIRTUAL,
        definition.getInternalName(),
        MethodNames.Converter$convert,
        definition.getConvertMethodDescriptor(),
        false);
    v.visitInsn(ARETURN);
    v.visitMaxs(-1, -1);
    v.visitEnd();
  }

  /**
   * 编写用于集合嵌套拷贝避免空指针错误的 lambda 方法
   * <pre>{@code
   * this::convert
   * }</pre>
   *
   * @param cw ClassWriter
   */
  private void genLambda$convert$0Method(
      ClassWriter cw
  ) {
    var convertDescriptor = definition.getConvertMethodDescriptor();
    var v = cw.visitMethod(
        ACC_PRIVATE | ACC_SYNTHETIC,
        MethodNames.lambda$convert$0,
        convertDescriptor,
        null,
        null
    );

    v.visitCode();
    v.visitVarInsn(ALOAD, 0);
    v.visitVarInsn(ALOAD, 1);
    v.visitMethodInsn(INVOKEVIRTUAL, definition.getInternalName(), MethodNames.Converter$convert, convertDescriptor, false);
    v.visitInsn(ARETURN);

    v.visitMaxs(-1, -1);
    v.visitEnd();
  }

}
