package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.annotation.Property;
import io.github.tanyaofei.beancopier.utils.Constants;
import io.github.tanyaofei.beancopier.utils.*;
import io.github.tanyaofei.beancopier.utils.Reflections.BeanProperty;
import io.github.tanyaofei.guava.common.reflect.TypeToken;
import org.objectweb.asm.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

class ConverterCodeWriter implements Opcodes, MethodConstants {

  private final Class<?> sc;
  private final Class<?> tc;
  private final ConverterConfiguration configuration;
  private final String internalName;
  private boolean hasNestedCollectionCopy = false;

  public ConverterCodeWriter(String internalName, Class<?> sc, Class<?> tc, ConverterConfiguration configuration) {
    this.sc = sc;
    this.tc = tc;
    this.configuration = configuration;
    this.internalName = internalName;
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
        CodeEmitter.getClassSignature(
            Constants.CLASS_INFO_OBJECT,
            ClassInfo.of(Converter.class, sc, tc)
        ),
        Constants.INTERNAL_NAME_OBJECT,
        Constants.INTERNAL_NAME_ARRAY_CONVERTER
    );
    cw.visitSource(Constants.SOURCE_FILE, null);
    writeNoArgsConstructor(cw);
    writeConvertMethod(cw);
    if (hasNestedCollectionCopy) {
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
    MethodVisitor v = cw.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, "<init>", "()V", null, null);
    v.visitCode();
    v.visitVarInsn(ALOAD, 0);
    CodeEmitter.invokeNoArgsConstructor(v, Object.class);
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
        CONVERTER$CONVERT.getName(),
        CodeEmitter.getMethodDescriptor(tc, sc),
        null,
        null
    );

    v.visitCode();

    CodeEmitter.newInstanceViaNoArgsConstructor(v, tc);
    v.visitVarInsn(ASTORE, 2);
    Label jumpHere = null;
    Iterable<BeanProperty> setters = Reflections.getBeanSetters(tc, configuration.isIncludingSuper());
    Map<String, BeanProperty> getters = BeanProperty.mapIterable(Reflections.getBeanGetters(sc, configuration.isIncludingSuper()));

    for (BeanProperty tbp : setters) {
      Field tf = tbp.getField();
      Property property = configuration.isPropertySupported()
          ? Optional.ofNullable(tf.getAnnotation(Property.class)).orElse(Constants.DEFAULT_PROPERTY)
          : Constants.DEFAULT_PROPERTY;
      if (property.skip()) {
        continue;
      }

      String name = property.value().isEmpty() ? tbp.getName() : property.value();
      BeanProperty sbp = getters.get(name);
      if (sbp == null) {
        continue;
      }

      Field sf = sbp.getField();
      Method getter = sbp.getXetter();
      Method setter = tbp.getXetter();

      if (configuration.isPreferNested() && isNestedCopy(sf, tf)) {
        if (jumpHere != null) {
          v.visitLabel(jumpHere);
        }
        jumpHere = skipNull(v, getter);
        nestedCopy(v, getter, setter);
      } else if (configuration.isPreferNested() && isNestedCollectionCopy(sf, tf)) {
        if (jumpHere != null) {
          v.visitLabel(jumpHere);
        }
        jumpHere = skipNull(v, getter);
        nestedCollectionCopy(v, getter, setter);
        hasNestedCollectionCopy = true;
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
        CONVERTER$CONVERT.getName(),
        Constants.METHOD_DESCRIPTOR_CONVERTER_CONVERT,
        null,
        null);
    v.visitCode();
    v.visitVarInsn(ALOAD, 0);
    v.visitVarInsn(ALOAD, 1);
    v.visitTypeInsn(CHECKCAST, Type.getInternalName(sc));
    v.visitMethodInsn(
        INVOKEVIRTUAL,
        internalName,
        CONVERTER$CONVERT.getName(),
        CodeEmitter.getMethodDescriptor(tc, sc),
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
        Constants.METHOD_NAME_LAMBDA$CONVERT$0,
        CodeEmitter.getMethodDescriptor(tc, sc),
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
    v.visitMethodInsn(INVOKEVIRTUAL, internalName, CONVERTER$CONVERT.getName(), CodeEmitter.getMethodDescriptor(tc, sc), false);
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
  private boolean isNestedCollectionCopy(Field sf, Field tf) {
    Class<?> sfc = sf.getType();
    Class<?> tfc = tf.getType();
    if (configuration.isFullTypeMatching()) {
      return Collection.class.isAssignableFrom(sfc)
          && sfc == tfc
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
      Method getter,
      Method setter
  ) {
    String methodDescriptor = CodeEmitter.getMethodDescriptor(tc, sc);
    v.visitVarInsn(ALOAD, 2);

    // this.getField()
    v.visitVarInsn(ALOAD, 1);
    CodeEmitter.invokeMethod(v, INVOKEVIRTUAL, getter);

    // list.stream()
    CodeEmitter.invokeMethod(v, INVOKEINTERFACE, LIST$STREAM);

    // (o) -> o == null ? null : this.convert(o)
    v.visitVarInsn(ALOAD, 0);
    v.visitInvokeDynamicInsn(
        FUNCTION$APPLY.getName(),
        "(L" + internalName + ";)" + Constants.TYPE_DESCRIPTOR_FUNCTION,
        new Handle(H_INVOKESTATIC,
            Constants.INTERNAL_NAME_LAMBDA_METAFACTORY,
            LAMBDA_META_FACTORY$METAFACOTRY.getName(),
            Constants.METHOD_DESCRIPTOR_LAMBDA_META_FACTORY_METAFACTORY,
            false),
        Constants.METHOD_TYPE_CONVERTER,
        new Handle(H_INVOKESPECIAL,
            internalName,
            Constants.METHOD_NAME_LAMBDA$CONVERT$0,
            methodDescriptor,
            false
        ),
        Type.getType(methodDescriptor)
    );

    // stream.map()
    CodeEmitter.invokeMethod(v, INVOKEINTERFACE, STREAM$MAP);

    // collector.toList()
    CodeEmitter.invokeMethod(v, INVOKESTATIC, COLLECTORS$TO_LIST);

    // stream.collect()
    CodeEmitter.invokeMethod(v, INVOKEINTERFACE, STREAM$COLLECT);

    // (List) .....
    v.visitTypeInsn(CHECKCAST, Constants.INTERNAL_NAME_LIST);

    // target.setField()
    CodeEmitter.invokeMethod(v, INVOKEVIRTUAL, setter);
    dropSetterReturnVal(v, setter);
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
      Method getter,
      Method setter
  ) {
    v.visitVarInsn(ALOAD, 2);
    v.visitVarInsn(ALOAD, 1);
    CodeEmitter.invokeMethod(v, INVOKEVIRTUAL, getter);
    CodeEmitter.invokeMethod(v, INVOKEVIRTUAL, setter);
    dropSetterReturnVal(v, setter);
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
  private Label skipNull(MethodVisitor v, Method getter) {
    Label label = new Label();
    v.visitVarInsn(ALOAD, 1);
    CodeEmitter.invokeMethod(v, INVOKEVIRTUAL, getter);
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
  private void nestedCopy(MethodVisitor v, Method getter, Method setter) {
    // target.setField(convert(source.getField()))
    v.visitVarInsn(ALOAD, 2);
    v.visitVarInsn(ALOAD, 0);
    v.visitVarInsn(ALOAD, 1);
    CodeEmitter.invokeMethod(v, INVOKEVIRTUAL, getter);
    v.visitMethodInsn(
        INVOKEVIRTUAL,
        internalName,
        CONVERTER$CONVERT.getName(),
        CodeEmitter.getMethodDescriptor(tc, sc),
        false);
    CodeEmitter.invokeMethod(v, INVOKEVIRTUAL, setter);
    dropSetterReturnVal(v, setter);
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

  /**
   * 如果目标字段的 setter 方法具有返回值，则将这个局部变量从操作数栈中移除以保证兼容；如果没有返回值，则不做任何事情
   *
   * @param v      MethodVisitor
   * @param setter 目标字段的 setter 方法
   */
  private void dropSetterReturnVal(MethodVisitor v, Method setter) {
    if (!setter.getReturnType().equals(Void.TYPE)) {
      v.visitInsn(POP);
    }
  }

}
