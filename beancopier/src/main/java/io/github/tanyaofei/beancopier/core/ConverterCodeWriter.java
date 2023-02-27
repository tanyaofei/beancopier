package io.github.tanyaofei.beancopier.core;

import io.github.tanyaofei.beancopier.annotation.Property;
import io.github.tanyaofei.beancopier.constants.InternalNames;
import io.github.tanyaofei.beancopier.constants.MethodDescriptors;
import io.github.tanyaofei.beancopier.constants.MethodNames;
import io.github.tanyaofei.beancopier.constants.Properties;
import io.github.tanyaofei.beancopier.converter.AbstractConverter;
import io.github.tanyaofei.beancopier.converter.Converter;
import io.github.tanyaofei.beancopier.core.annotation.Feature;
import io.github.tanyaofei.beancopier.core.instanter.AllArgsConstructorInstanter;
import io.github.tanyaofei.beancopier.core.instanter.NoArgsConstructorInstanter;
import io.github.tanyaofei.beancopier.core.local.LocalDefiner;
import io.github.tanyaofei.beancopier.core.local.LocalDefiners;
import io.github.tanyaofei.beancopier.core.local.LocalDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalsDefinitionContext;
import io.github.tanyaofei.beancopier.utils.ClassSignature;
import io.github.tanyaofei.beancopier.utils.reflection.Reflections;
import io.github.tanyaofei.beancopier.utils.reflection.member.BeanMember;
import org.objectweb.asm.*;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Bytecode writer for writing converter class
 */
public class ConverterCodeWriter implements Opcodes {

  private static final LocalDefiner definer = LocalDefiners.getDefiner();
  private static final String SOURCE_FILE = "<generated>";

  @Nonnull
  private final ConverterDefinition definition;

  public ConverterCodeWriter(@Nonnull ConverterDefinition definition) {
    this.definition = definition;
  }

  /**
   * generate a no-args-constructor
   *
   * @param cw ClassWriter
   */
  private void genConstructor(@Nonnull ClassWriter cw) {
    var v = cw.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, MethodNames.Object$init, MethodDescriptors.Object$init, null, null);
    v.visitCode();
    v.visitVarInsn(ALOAD, 0);
    v.visitMethodInsn(INVOKESPECIAL, InternalNames.AbstractConverter, MethodNames.Object$init, MethodDescriptors.Object$init, false);
    v.visitInsn(RETURN);
    v.visitMaxs(-1, -1);
    v.visitEnd();
  }

  /**
   * generate converter bytecode
   *
   * @return converter bytecode
   */
  public byte[] write() {
    var sourceType = definition.getSourceType();
    var tc = definition.getTargetType();
    var cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    cw.visit(
        V17,
        ACC_PUBLIC,
        definition.getInternalName(),
        ClassSignature.getClassSignature(
            ClassSignature.ClassInfo.of(AbstractConverter.class, sourceType, tc)
        ),
        InternalNames.AbstractConverter,
        null
    );
    cw.visitSource(SOURCE_FILE, null);
    genConstructor(cw);
    genConvertMethod(cw);
    genConfigurationAnnotation(cw);
    if (sourceType != Object.class || tc != Object.class) {
      genConvertBridgeMethod(cw);
    }

    return cw.toByteArray();
  }

  /**
   * <pre>{@code
   * if (source == nul) {
   *   return null;
   * }
   * }</pre>
   *
   * @param v method writer
   */
  private void returnNullIfNull(@Nonnull MethodVisitor v) {
    var ifNonNull = new Label();
    v.visitVarInsn(ALOAD, 1);
    v.visitJumpInsn(IFNONNULL, ifNonNull);
    v.visitInsn(ACONST_NULL);
    v.visitInsn(ARETURN);
    v.visitLabel(ifNonNull);
  }

  /**
   * Write {@link Converter#convert(Object)} method
   * <pre>{@code
   * var var2 = source.getVar2();
   * var var3 = source.getVar3();
   * }</pre>
   *
   * <ul>
   *   <li>if target is {@link Record} or provides an all-args-constructor</li>
   *   <pre>{@code
   *   var target = new Target(var2, var3);
   *   }
   *   </pre>
   *   <li>otherwise</li>
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
  private void genConvertMethod(@Nonnull ClassWriter cw) {
    var configuration = definition.getFeatures();
    var mv = cw.visitMethod(
        ACC_PUBLIC,
        MethodNames.Converter$convert,
        definition.getConvertMethodDescriptor(),
        null,
        null
    );
    mv.visitCode();

    returnNullIfNull(mv);

    var consumers = Reflections.getMembersWithSetter(definition.getTargetType(), configuration.isIncludingSuper());
    var providers = BeanMember.mapIterable(Reflections.getGettableBeanMember(definition.getSourceType(), configuration.isIncludingSuper()));

    int firstLocalStore = 2;  // 0: this, 1: source object ref
    var context = new LocalsDefinitionContext(providers, firstLocalStore);

    for (var member : consumers) {
      var property = configuration.isPropertySupported()
          ? Properties.getOrDefault(member)
          : Properties.defaultProperty;
      definer.define(
          mv,
          definition,
          LocalDefinition
              .builder()
              .name(getLocalDefinitionName(property, member.getName()))
              .type(member.getType())
              .skip(property.skip())
              .build(),
          context
      );
    }

    int targetStore = context.getNextStore();
    var instancer = switch (definition.getInstantiateMode()) {
      case ALL_ARGS_CONSTRUCTOR -> new AllArgsConstructorInstanter(
          mv,
          definition,
          targetStore,
          consumers,
          firstLocalStore
      );
      case NO_ARGS_CONSTRUCTOR -> new NoArgsConstructorInstanter(
          mv,
          definition,
          targetStore,
          consumers,
          StreamSupport
              .stream(consumers.spliterator(), true)
              .filter(m -> (configuration.isPropertySupported()
                  ? Properties.getOrDefault(m)
                  : Properties.defaultProperty).skip())
              .collect(Collectors.toSet()),
          firstLocalStore
      );
    };
    instancer.instantiate();

    mv.visitVarInsn(ALOAD, targetStore);
    mv.visitInsn(ARETURN);
    mv.visitMaxs(-1, -1);
    mv.visitEnd();
  }

  /**
   * Return a local definition name
   *
   * @param property    The {@link Property} annotated on POJO field or Record component
   * @param defaultName Default name when {@link Property} hasn't alias
   * @return local definition name
   */
  @Nonnull
  private String getLocalDefinitionName(@Nonnull Property property, @Nonnull String defaultName) {
    if (definition.isClone()) {
      return defaultName;
    }

    for (var alias : property.alias()) {
      for (var type : alias.forType()) {
        if (definition.getSourceType() == type) {
          return alias.value();
        }
      }
    }

    if (property.value().length() > 0) {
      return property.value();
    }

    return defaultName;
  }

  /**
   * Write the convert generic bridge method
   * <pre>{@code
   * public Object convert(Object source) {
   *   return convert((Source) source);
   * }
   * }</pre>
   *
   * @param cw class writer
   */
  private void genConvertBridgeMethod(
      @Nonnull ClassWriter cw
  ) {
    var mv = cw.visitMethod(
        ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC,
        MethodNames.Converter$convert,
        MethodDescriptors.Converter$convert,
        null,
        null
    );
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitTypeInsn(CHECKCAST, org.objectweb.asm.Type.getInternalName(definition.getSourceType()));
    mv.visitMethodInsn(
        INVOKEVIRTUAL,
        definition.getInternalName(),
        MethodNames.Converter$convert,
        definition.getConvertMethodDescriptor(),
        false
    );
    mv.visitInsn(ARETURN);
    mv.visitMaxs(-1, -1);
    mv.visitEnd();
  }

  /**
   * Add {@link Feature} to converter
   *
   * @param cw class writer
   */
  private void genConfigurationAnnotation(@Nonnull ClassWriter cw) {
    var av = cw.visitAnnotation(Type.getDescriptor(Feature.class), false);
    var configuration = definition.getFeatures();
    av.visit(Feature.SKIP_NULL, configuration.isSkipNull());
    av.visit(Feature.PREFER_NESTED, configuration.isPreferNested());
    av.visit(Feature.PROPERTY_SUPPORTED, configuration.isPropertySupported());
    av.visit(Feature.FULL_TYPE_MATCHING, configuration.isFullTypeMatching());
    av.visit(Feature.INCLUDING_SUPER, configuration.isIncludingSuper());
  }

}
