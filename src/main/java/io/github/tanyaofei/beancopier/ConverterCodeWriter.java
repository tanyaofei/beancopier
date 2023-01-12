package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.annotation.Property;
import io.github.tanyaofei.beancopier.utils.ClassSignature;
import io.github.tanyaofei.beancopier.utils.ClassSignature.ClassInfo;
import io.github.tanyaofei.beancopier.utils.Reflections;
import io.github.tanyaofei.beancopier.utils.Reflections.BeanProperty;
import io.github.tanyaofei.guava.common.reflect.TypeToken;
import org.objectweb.asm.*;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 转换器字节码编写工具
 *
 * @since 0.1.5
 */
class ConverterCodeWriter implements Opcodes, Methods {

  private final Class<?> sc;
  private final Class<?> tc;
  private final ConverterConfiguration configuration;
  private final String internalName;
  private boolean containsCollectionNestedCopies = false;
  private final String methodDescriptor;

  public ConverterCodeWriter(String internalName, Class<?> sc, Class<?> tc, ConverterConfiguration configuration) {
    this.sc = sc;
    this.tc = tc;
    this.configuration = configuration;
    this.internalName = internalName;
    this.methodDescriptor = Type.getMethodDescriptor(Type.getType(tc), Type.getType(sc));
  }

  /**
   * 编写 Converter 字节码
   *
   * @return Converter 字节码字节数组
   */
  public byte[] write() {
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    cw.visit(
        V1_8,
        ACC_PUBLIC | ACC_SYNTHETIC,
        internalName,
        ClassSignature.getClassSignature(
            ClassInfos.Object,
            ClassInfo.of(Converter.class, sc, tc)
        ),
        InternalNames.Object,
        InternalNames.aConverter
    );
    cw.visitSource("<generated>", null);
    writeNoArgsConstructor(cw);
    writeConvertMethod(cw);
    if (containsCollectionNestedCopies) {
      writeLambda$convert$0Method(cw);
    }
    if (sc != Object.class || tc != Object.class) {
      writeConvertBridgeMethod(cw);
    }

    return cw.toByteArray();
  }

  /**
   * 编写无参构造函数
   *
   * @param cw ClassWriter
   */
  private void writeNoArgsConstructor(ClassWriter cw) {
    MethodVisitor v = cw.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, MethodNames.Object$init, MethodDescriptors.Object$init, null, null);
    v.visitCode();
    v.visitVarInsn(ALOAD, 0);
    v.visitMethodInsn(INVOKESPECIAL, InternalNames.Object, MethodNames.Object$init, MethodDescriptors.Object$init, false);
    v.visitInsn(RETURN);
    v.visitMaxs(-1, -1);
    v.visitEnd();
  }

  /**
   * 编写转换方法 {@link Converter#convert(Object)}
   *
   * @param cw ClassWriter
   */
  private void writeConvertMethod(ClassWriter cw) {
    MethodVisitor v = cw.visitMethod(
        ACC_PUBLIC,
        MethodNames.Converter$convert,
        methodDescriptor,
        null,
        null
    );

    v.visitCode();

    MethodInvoker.constructorInvoker(tc).invoke(v);
    v.visitVarInsn(ASTORE, 2);
    Label jumpHere = null;
    Iterable<BeanProperty> setters = Reflections.getBeanSetters(tc, configuration.isIncludingSuper());
    Map<String, BeanProperty> getters = BeanProperty.mapIterable(Reflections.getBeanGetters(sc, configuration.isIncludingSuper()));

    for (BeanProperty tbp : setters) {
      Field tf = tbp.getField();
      Property property = configuration.isPropertySupported()
          ? Optional.ofNullable(tf.getAnnotation(Property.class)).orElse(Property.DEFAULT)
          : Property.DEFAULT;
      if (property.skip()) {
        continue;
      }

      String name = property.value().isEmpty() ? tbp.getName() : property.value();
      BeanProperty sbp = getters.get(name);
      if (sbp == null) {
        continue;
      }

      Field sf = sbp.getField();
      MethodInvoker getter = MethodInvoker.methodInvoker(sbp.getXetter());
      MethodInvoker setter = MethodInvoker.methodInvoker(tbp.getXetter());

      if (configuration.isPreferNested() && isNestedCopy(sf, tf)) {
        if (jumpHere != null) {
          v.visitLabel(jumpHere);
        }
        jumpHere = skipNull(v, getter);
        nestedCopy(v, getter, setter);
      } else if (configuration.isPreferNested() && isCollectionNestedCopy(sf, tf)) {
        if (jumpHere != null) {
          v.visitLabel(jumpHere);
        }
        jumpHere = skipNull(v, getter);
        nestedCollectionCopy(v, getter, setter);
        containsCollectionNestedCopies = true;
      } else if (isCompatible(sf, tf)) {
        if (jumpHere != null) {
          v.visitLabel(jumpHere);
        }
        jumpHere = configuration.isSkipNull() ? skipNull(v, getter) : null;
        compatibleCopy(v, getter, setter);
      }
    }

    if (jumpHere != null) {
      v.visitLabel(jumpHere);
    }
    v.visitVarInsn(ALOAD, 2);
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
  private void writeConvertBridgeMethod(
      ClassWriter cw
  ) {
    MethodVisitor v = cw.visitMethod(
        ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC,
        MethodNames.Converter$convert,
        MethodDescriptors.Converter$convert,
        null,
        null);
    v.visitCode();
    v.visitVarInsn(ALOAD, 0);
    v.visitVarInsn(ALOAD, 1);
    v.visitTypeInsn(CHECKCAST, Type.getInternalName(sc));
    v.visitMethodInsn(
        INVOKEVIRTUAL,
        internalName,
        MethodNames.Converter$convert,
        methodDescriptor,
        false);
    v.visitInsn(ARETURN);
    v.visitMaxs(-1, -1);
    v.visitEnd();
  }

  /**
   * 编写用于集合嵌套拷贝避免空指针错误的 lambda 方法
   * <pre>{@code
   * (Function)(varx1) -> {return varx1 == null ? null : this.convert(varx1);};
   * }</pre>
   *
   * @param cw ClassWriter
   */
  private void writeLambda$convert$0Method(
      ClassWriter cw
  ) {
    MethodVisitor v = cw.visitMethod(
        ACC_PRIVATE | ACC_SYNTHETIC,
        MethodNames.lambda$convert$0,
        methodDescriptor,
        null,
        null
    );

    v.visitCode();

    Label ifnonnullLabel = new Label();
    Label gotoLabel = new Label();

    v.visitVarInsn(ALOAD, 1);
    v.visitJumpInsn(IFNONNULL, ifnonnullLabel);
    v.visitInsn(ACONST_NULL);
    v.visitJumpInsn(GOTO, gotoLabel);
    v.visitLabel(ifnonnullLabel);
    v.visitVarInsn(ALOAD, 0);
    v.visitVarInsn(ALOAD, 1);
    v.visitMethodInsn(INVOKEVIRTUAL, internalName, MethodNames.Converter$convert, methodDescriptor, false);
    v.visitLabel(gotoLabel);
    v.visitInsn(ARETURN);

    v.visitMaxs(-1, -1);
    v.visitEnd();
  }

  /**
   * 判断类型是否兼容
   *
   * @param sf 来源字段
   * @param tf 目标字段
   * @return 如果配置了全类型匹配，则严格判断两者的类型是否完全一致；否则会根据 JAVA 的规范判断是否兼容
   */
  private boolean isCompatible(Field sf, Field tf) {
    return configuration.isFullTypeMatching()
        ? sf.getGenericType().equals(tf.getGenericType())
        : TypeToken.of(sf.getGenericType()).isSubtypeOf(tf.getGenericType());
  }

  /**
   * 判断是否嵌套拷贝
   *
   * @param sf 来源字段
   * @param tf 目标字段
   * @return 如果配置了全类型匹配，则严格判断两者的类型是否完全一致；否则根据 JAVA 的规范判断是否兼容
   */
  private boolean isCollectionNestedCopy(Field sf, Field tf) {
    Class<?> sfc = sf.getType();
    Class<?> tfc = tf.getType();
    if (configuration.isFullTypeMatching()) {
      return sfc == tfc
          && Collection.class.isAssignableFrom(tfc)
          && getCollectionElementType(sf) == sc
          && getCollectionElementType(tf) == tc;
    } else {
      return Collection.class.isAssignableFrom(sfc)
          && Collection.class.isAssignableFrom(tfc)
          && getCollectionElementType(sf) == sc
          && getCollectionElementType(tf) == tc;
    }
  }

  /**
   * 集合嵌套拷贝
   *
   * @param v      MethodVisitor
   * @param getter 来源字段的 getter 方法
   * @param setter 目标字段的 setter 方法
   */
  private void nestedCollectionCopy(
      MethodVisitor v,
      MethodInvoker getter,
      MethodInvoker setter
  ) {
    v.visitVarInsn(ALOAD, 2);

    // this.getField()
    v.visitVarInsn(ALOAD, 1);
    getter.invoke(v);

    // list.stream()
    MethodInvokers.List$stream.invoke(v);

    // (o) -> o == null ? null : this.convert(o)
    v.visitVarInsn(ALOAD, 0);
    v.visitInvokeDynamicInsn(
        MethodNames.Function$apply,
        "(L" + internalName + ";)" + TypeDescriptors.Function,
        new Handle(H_INVOKESTATIC,
            InternalNames.LambdaMetafactory,
            MethodNames.Lambda$MetaFactory$metafactory,
            MethodDescriptors.LambdaMetafactory$metafactory,
            false),
        MethodTypes.Converter$convert,
        new Handle(H_INVOKESPECIAL,
            internalName,
            MethodNames.lambda$convert$0,
            methodDescriptor,
            false
        ),
        Type.getType(methodDescriptor)
    );

    // stream.map()
    MethodInvokers.Stream$map.invoke(v);

    // collector.toList()
    MethodInvokers.Collector$toList.invoke(v);

    // stream.collect()
    MethodInvokers.Stream$collect.invoke(v);

    // (List) .....
    v.visitTypeInsn(CHECKCAST, InternalNames.List);

    // target.setField()
    setter.invoke(v, true);
  }

  /**
   * 类型兼容拷贝
   * <pre>{@code
   * var2.setValue(var1.getValue());
   * }</pre>
   *
   * @param v      MethodVisit
   * @param getter 来源字段的 getter 方法
   * @param setter 目标字段的 setter 方法
   */
  private void compatibleCopy(
      MethodVisitor v,
      MethodInvoker getter,
      MethodInvoker setter
  ) {
    v.visitVarInsn(ALOAD, 2);
    v.visitVarInsn(ALOAD, 1);
    getter.invoke(v);
    setter.invoke(v, true);
  }

  /**
   * 获取指定字段 {@link Collection} 的元素类型
   *
   * @param f 集合字段
   * @return 集合的元素类型
   */
  @SuppressWarnings("unchecked")
  private Class<?> getCollectionElementType(Field f) {
    return ((TypeToken<? extends Collection<?>>) TypeToken.of(f.getGenericType()))
        .getSupertype(Collection.class)
        .resolveType(Collection.class.getTypeParameters()[0])
        .getRawType();
  }

  /**
   * 创建一个用于 ifnull 跳转的 Label, 这个 Label 需要被 <pre>{@code v.visitLabel(label)}</pre> 才能明确跳转的行号
   *
   * @param v      MethodVisitor
   * @param getter 来源字段的 getter 方法
   * @return 用于 ifnull 跳转的 Label
   */
  private Label skipNull(MethodVisitor v, MethodInvoker getter) {
    Label label = new Label();
    v.visitVarInsn(ALOAD, 1);
    getter.invoke(v);
    v.visitJumpInsn(IFNULL, label);
    return label;
  }

  /**
   * 嵌套拷贝
   *
   * @param v      MethodVisitor
   * @param getter 来源字段的 getter 方法
   * @param setter 来源字段的 setter 方法
   */
  private void nestedCopy(MethodVisitor v, MethodInvoker getter, MethodInvoker setter) {
    // target.setField(convert(source.getField()))
    v.visitVarInsn(ALOAD, 2);
    v.visitVarInsn(ALOAD, 0);
    v.visitVarInsn(ALOAD, 1);
    getter.invoke(v);
    v.visitMethodInsn(
        INVOKEVIRTUAL,
        internalName,
        MethodNames.Converter$convert,
        methodDescriptor,
        false);
    setter.invoke(v, true);
  }

  /**
   * 是否嵌套拷贝
   *
   * @param sf 来源字段
   * @param tf 目标字段
   * @return 是否嵌套拷贝
   */
  private boolean isNestedCopy(Field sf, Field tf) {
    return sc == sf.getType() && tc == tf.getType();
  }

}
