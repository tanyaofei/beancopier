package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.annotation.Feature;
import io.github.tanyaofei.beancopier.annotation.Property;
import io.github.tanyaofei.beancopier.exception.BeanCopierException;
import io.github.tanyaofei.beancopier.typehandler.TypeHandler;
import io.github.tanyaofei.beancopier.utils.*;
import lombok.SneakyThrows;
import org.objectweb.asm.Type;
import org.objectweb.asm.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.reflect.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 使用 ASM 字节码技术在运行时创建 Source  to  Target 的转换器字节码
 *
 * @author tanyaofei
 */
public class ConverterFactory implements Opcodes, MethodConstants {

  /**
   * 转换器生成计数器
   */
  private static final AtomicInteger COUNTER = new AtomicInteger();

  private final ConverterClassLoader classLoader;
  private final String pkg;
  private final String classDumpPath;

  public ConverterFactory(ConverterClassLoader classLoader, String pkg, String classDumpPath) {
    this.classLoader = classLoader;
    this.pkg = pkg;
    this.classDumpPath = classDumpPath;
  }

  /**
   * 创建 sType  to  tType 的转换器并加载到运行时内存中并创建实例
   *
   * @param sType 拷贝来源类
   * @param tType 拷贝目标类
   * @param <S>   拷贝来源
   * @param <T>   拷贝目标
   * @return sType  to  tType 转换器实例
   */
  @SuppressWarnings("unchecked")
  public <S, T> Converter<S, T> generateConverter(
          Class<S> sType, Class<T> tType
  ) {
    // 检查 sType 是一个 public class, 因为无法 import 一个非 public 的类(default 不考虑)
    if (!Modifier.isPublic(sType.getModifiers())) {
      throw new IllegalArgumentException(String.format("Class '%s' is not a public class", sType));
    }
    // 检查 sType 是一个 public class, 因为无法 import 一个非 public 的类(default 不考虑)
    if (!Modifier.isPublic(tType.getModifiers())) {
      throw new IllegalArgumentException(String.format("Class '%s' is not a public class", tType));
    }
    // 检查 tType 提供一个 public 无参构造函数
    try {
      if (!Modifier.isPublic(tType.getConstructor().getModifiers())) {
        throw new IllegalArgumentException(String.format(
            "Can not access the no args construct of class '%s', make it public", tType));
      }
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException("Can not find the no-args-constructor of class: " + tType);
    }

    // 创建类编写器, 自动完成局部变量计数和栈深度计数
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    String internalName = genConverterInternalName(sType, tType);
    // 创建转换器类
    cw.visit(V1_8, ACC_PUBLIC | ACC_SYNTHETIC,
        internalName,
        ReflectUtils.getClassSignature(
            ClassInfo.of(Object.class),
            ClassInfo.of(Converter.class, sType, tType)),
        Type.getInternalName(Object.class),
        new String[]{Type.getInternalName(Converter.class)});

    // 编写构造函数
    writeNoArgsConstructor(cw);
    // 编写 convert 实现方法
    writeConvertMethod(cw, internalName, sType, tType);
    // 编写 convert 抽象方法
    writeConvertBridgeMethod(cw, internalName, sType, tType);

    // 加载到内存并实例化
    final byte[] code = cw.toByteArray();
    Class<Converter<S, T>> clazz = (Class<Converter<S, T>>) classLoader.defineClass(null, code);
    dumpClassIfNeeded(code, clazz.getSimpleName());
    try {
      return clazz.getConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new BeanCopierException("Failed to initialize converter", e);
    }
  }

  private void dumpClassIfNeeded(byte[] code, String className) {
    if (StringUtils.hasLength(classDumpPath)) {
      try (FileOutputStream out = new FileOutputStream(classDumpPath + File.separator + className + ".class")) {
        out.write(code);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 生成转换器的类名, 为了防止类名(SimpleName)重复, 因此在类名最后添加了一个计数防止生成的转换器类名重复
   *
   * @param sType 拷贝目标类
   * @param tType 拷贝目标类
   * @return 生成的转换器类名
   */
  private String genConverterInternalName(Class<?> sType, Class<?> tType) {
    return pkg
            + "/"
            + sType.getSimpleName()
            + "To"
            + tType.getSimpleName()
            + "Converter$GeneratedByBeanCopier$"
            + COUNTER.getAndIncrement();
  }

  /**
   * 编写无参构造方法
   * <ol>
   *   <li>aload 0  // load this</li>
   *   <li>invokespecial java/lang/Object#!&lt;init&gt;() // init Converter</li>
   *   <li>return</li>
   * </ol>
   *
   * @param cw 类编写器
   */
  private void writeNoArgsConstructor(ClassWriter cw) {
    MethodVisitor visitor = cw.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, "<init>", "()V", null, null);
    visitor.visitCode();
    // this 入栈
    visitor.visitVarInsn(ALOAD, 0);
    // 调用 Object 的构造方法
    BytecodeUtils.invokeNoArgsConstructor(visitor, Object.class);
    // 结束方法
    visitor.visitInsn(RETURN);
    visitor.visitMaxs(-1, -1);
    visitor.visitEnd();
  }

  /**
   * 编写 convert 的桥接方法
   * <ol>
   *   <li>aload 0  // load this<li>
   *   <li>aload 1  // load source<li>
   *   <li>checkcase Source.class // case source to Source.class => source = (Source) source<li>
   *   <li>invokevirtual convert()  // invoke method convert() => this.convert(target) <li>
   *   <li>areturn // return return-value from convert()<li>
   * </ol>
   *
   * @param cw           类编写器
   * @param internalName 转换器 JAVA 内部名称
   * @param sType        拷贝来源类
   * @param tType        拷贝目标类
   */
  private <S, T> void writeConvertBridgeMethod(
      ClassWriter cw, String internalName, Class<S> sType, Class<T> tType
  ) {
    MethodVisitor visitor = cw.visitMethod(
        ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC,
        CONVERTER$CONVERT.getName(),
        Type.getMethodDescriptor(CONVERTER$CONVERT),
        null,
        null);
    visitor.visitCode();
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitVarInsn(ALOAD, 1);
    visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(sType));
    visitor.visitMethodInsn(
        INVOKEVIRTUAL,
        internalName,
        CONVERTER$CONVERT.getName(),
        ReflectUtils.getMethodDescriptor(tType, sType),
        false);
    visitor.visitInsn(ARETURN);
    visitor.visitMaxs(-1, -1);
    visitor.visitEnd();
  }

  /**
   * 编写实现 Convert#convert(Source source, Target target) 方法
   * <ol>
   *   <li>new Target</li>
   *   <li>dup</li>
   *   <li>invokespecial Target#&lt;init&gt;()</li>
   *   <li>astore 2</li>
   *   <li>
   *     <ol>循环 target 的每一个字段
   *       <li>aload 2  // load target</li>
   *       <li>aload 1  // load source</li>
   *       <li>invokevirtual target#getField()</li>
   *       <li>invokevirtual target#setField()</li>
   *       <li>pop</li>
   *    </ol>
   *   </li>
   *   <li>aload 2  // load target</li>
   *   <li>areturn // return target</li>
   * </ol>
   *
   * @param cw    类编写器
   * @param sType 拷贝来源类
   * @param tType 拷贝目标类
   * @see Converter#convert(Object)
   */
  private <S, T> void writeConvertMethod(
      ClassWriter cw,
      String internalName,
      Class<S> sType,
      Class<T> tType
  ) {
    MethodVisitor visitor = cw.visitMethod(
        ACC_PUBLIC,
        CONVERTER$CONVERT.getName(),
        ReflectUtils.getMethodDescriptor(tType, sType),
        null,
        null);

    visitor.visitCode();

    // -- Target target = new Target()
    // new
    // dup
    // invokespecial #Converter <init>()V;
    // astore_2
    visitor.visitTypeInsn(NEW, Type.getInternalName(tType));
    visitor.visitInsn(DUP);
    BytecodeUtils.invokeNoArgsConstructor(visitor, tType);
    visitor.visitVarInsn(ASTORE, 2);
    // -- end

    Label skipIfNull = null;
    Map<String, Tuple<Field, Method>> getters = ReflectUtils.getFieldGetters(sType);
    for (Map.Entry<String, Tuple<Field, Method>> setterEntry : ReflectUtils.getFieldSetters(tType).entrySet()) {
      Method setter = setterEntry.getValue().getT2();
      Field tField = setterEntry.getValue().getT1();

      String fieldName;
      Property property = tField.getAnnotation(Property.class);
      if (property == null || property.value().isEmpty()) {
        fieldName = setterEntry.getKey();
      } else {
        fieldName = property.value();
      }

      Tuple<Field, Method> filedGetter = getters.get(fieldName);
      if (filedGetter == null) {
        // target 没有同名字段跳过
        continue;
      }
      Method getter = filedGetter.getT2();
      Field sField = filedGetter.getT1();

      Class<? extends TypeHandler<?, ?>> typeHandler;
      if (isRecursionCopy(sType, sField, tType, tField)) {
        // 单个递归
        if (skipIfNull != null) {
          visitor.visitLabel(skipIfNull);
        }
        skipIfNull = skipFieldIfNull(visitor, getter);
        copyRecursionField(visitor, internalName, sType, getter, tType, setter);
      } else if (isListRecursionCopy(sType, sField, tType, tField)) {
        // 列表递归
        if (skipIfNull != null) {
          visitor.visitLabel(skipIfNull);
        }
        skipIfNull = skipFieldIfNull(visitor, getter);
        copyListRecursionField(visitor, internalName, sType, getter, tType, setter);
      } else if (isTypeCompatible(sField, tField)) {
        // 正常字段
        if (skipIfNull != null) {
          visitor.visitLabel(skipIfNull);
          skipIfNull = null;
        }
        copyNormalField(visitor, getter, setter);
      } else if ((typeHandler = getTypeHandler(sField, tField)) != null) {
        // 类型处理器处理字段
        if (skipIfNull != null) {
          visitor.visitLabel(skipIfNull);
        }
        skipIfNull = skipFieldIfNull(visitor, getter);
        copyHandledType(visitor, typeHandler, getter, setter);
      } else if (property != null && Feature.AUTO_BOXING_AND_UNBOXING.isSet(property)) {
        if (isBoxing(sField, tField)) {
          if (skipIfNull != null) {
            visitor.visitLabel(skipIfNull);
          }
          skipIfNull = null;
          copyUnboxedField(visitor, sField, tField, getter, setter);
        } else if (isUnboxing(sField, tField)) {
          if (skipIfNull != null) {
            visitor.visitLabel(skipIfNull);
          }
          skipIfNull = skipFieldIfNull(visitor, getter);  // 如果来源字段为 null 则结果字段为零值
          copyBoxedField(visitor, sField, tField, getter, setter);
        }
      }
    }

    // return target
    if (skipIfNull != null) {
      visitor.visitLabel(skipIfNull);
    }
    visitor.visitVarInsn(ALOAD, 2);
    visitor.visitInsn(ARETURN);
    visitor.visitMaxs(-1, -1);
    visitor.visitEnd();
  }

  private void copyBoxedField(
          MethodVisitor visitor,
          Field sField,
          Field tField,
          Method getter,
          Method setter
  ) {
    visitor.visitVarInsn(ALOAD, 2);
    visitor.visitVarInsn(ALOAD, 1);
    BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, getter);
    java.lang.reflect.Type tType = tField.getType();
    if (tType == int.class) {
      BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, NUMBER$INT_VALUE);
    } else if (tType == short.class) {
      BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, NUMBER$SHORT_VALUE);
    } else if (tType == long.class) {
      BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, NUMBER$LONG_VALUE);
    } else if (tType == float.class) {
      BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, NUMBER$FLOAT_VALUE);
    } else if (tType == double.class) {
      BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, NUMBER$DOUBLE_VALUE);
    } else if (tType == char.class) {
      BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, CHARACTER$CHAR_VALUE);
    } else if (tType == boolean.class) {
      BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, BOOLEAN$BOOLEAN_VALUE);
    } else if (tType == byte.class) {
      BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, BYTE$BYTE_VALUE);
    }

    BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, setter);
    dropSetterReturnVal(visitor, setter);
  }

  private void copyUnboxedField(
          MethodVisitor visitor,
          Field sField,
          Field tField,
          Method getter,
          Method setter
  ) {
    visitor.visitVarInsn(ALOAD, 2);
    visitor.visitVarInsn(ALOAD, 1);
    BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, getter);

    java.lang.reflect.Type tType = tField.getType();
    if (tType == Integer.class) {
      BytecodeUtils.invokeMethod(visitor, INVOKESTATIC, MethodConstants.INTEGER$VALUE_OF);
    } else if (tType == Short.class) {
      BytecodeUtils.invokeMethod(visitor, INVOKESTATIC, MethodConstants.SHORT$VALUE_OF);
    } else if (tType == Long.class) {
      BytecodeUtils.invokeMethod(visitor, INVOKESTATIC, MethodConstants.LONG$VALUE_OF);
    } else if (tType == Byte.class) {
      BytecodeUtils.invokeMethod(visitor, INVOKESTATIC, MethodConstants.BYTE$VALUE_OF);
    } else if (tType == Character.class) {
      BytecodeUtils.invokeMethod(visitor, INVOKESTATIC, MethodConstants.CHARACTER$VALUE_OF);
    } else if (tType == Float.class) {
      BytecodeUtils.invokeMethod(visitor, INVOKESTATIC, MethodConstants.FLOAT$VALUE_OF);
    } else if (tType == Double.class) {
      BytecodeUtils.invokeMethod(visitor, INVOKESTATIC, MethodConstants.DOUBLE$VALUE_OF);
    } else if (tType == Boolean.class) {
      BytecodeUtils.invokeMethod(visitor, INVOKESTATIC, MethodConstants.BOOLEAN$VALUE_OF);
    } else {
      throw new IllegalStateException("Unsupported type: " + tField.getType());
    }

    BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, setter);
    dropSetterReturnVal(visitor, setter);
  }


  /**
   * 拷贝普通字段
   *
   * @param visitor 方法编写器
   * @param getter  拷贝来源字段 getter 方法
   * @param setter  拷贝目标字段 setter
   */
  private void copyNormalField(
          MethodVisitor visitor,
          Method getter,
          Method setter
  ) {
    visitor.visitVarInsn(ALOAD, 2);
    visitor.visitVarInsn(ALOAD, 1);
    BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, getter);
    BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, setter);
    dropSetterReturnVal(visitor, setter);
  }

  /**
   * 使用类型处理器进行字段拷贝
   *
   * @param visitor     方法编写器
   * @param typeHandler 类型处理器类
   * @param getter      拷贝来源字段 getter 方法
   * @param setter      拷贝目标字段 setter 方法
   * @since 0.1.0
   */
  @SneakyThrows
  private void copyHandledType(
          MethodVisitor visitor,
          Class<? extends TypeHandler<?, ?>> typeHandler,
          Method getter,
          Method setter
  ) {
    visitor.visitVarInsn(ALOAD, 2);
    BytecodeUtils.createObjectWithNoArgsConstructor(visitor, typeHandler);

    visitor.visitVarInsn(ALOAD, 1);
    Method handleMethod = typeHandler.getMethod(TypeHandler.METHOD_NAME, getter.getReturnType());
    BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, getter);
    BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, handleMethod);
    BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, setter);
    dropSetterReturnVal(visitor, setter);
  }

  /**
   * 编写拷贝递归字段的 Code
   * <p>target.setField(convert(source.getField()))</p>
   * <pre>
   *   aload_2  // target
   *   aload_0  // this
   *   aload_1  // source
   *   invokevirtual #Source getField()
   *   invokevirtual #Converter convert()
   *   invokevirtual #Target setField()
   * </pre>
   *
   * @param visitor      方法编写器
   * @param internalName 所属类 internalName
   * @param sType        拷贝来源类
   * @param getter       拷贝来源字段 getter 方法
   * @param tType        拷贝目标类
   * @param setter       拷贝目标字段 setter 方法
   */
  private void copyRecursionField(
          MethodVisitor visitor,
          String internalName,
          Class<?> sType, Method getter,
          Class<?> tType, Method setter
  ) {
    // target.setField(convert(source.getField()))
    visitor.visitVarInsn(ALOAD, 2);
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitVarInsn(ALOAD, 1);
    BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, getter);
    visitor.visitMethodInsn(
            INVOKEVIRTUAL,
            internalName,
            CONVERTER$CONVERT.getName(),
            ReflectUtils.getMethodDescriptor(tType, sType),
            false);
    BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, setter);
    dropSetterReturnVal(visitor, setter);
  }

  /**
   * 编写拷贝递归字段(List)的 Code
   * <p>target.setField(source.getField().stream().map(this::convert).collect(Collectors.toList()))</p>
   *
   * @param visitor      方法编写器
   * @param internalName 所属类 internalName
   * @param sType        拷贝来源类
   * @param getter       拷贝来源字段 getter 方法
   * @param tType        拷贝目标类
   * @param setter       拷贝目标字段 setter 方法
   */
  private void copyListRecursionField(
      MethodVisitor visitor,
      String internalName,
      Class<?> sType, Method getter,
      Class<?> tType, Method setter
  ) {
    visitor.visitVarInsn(ALOAD, 2);

    // this.getField()
    visitor.visitVarInsn(ALOAD, 1);
    BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, getter);

    // list.stream()
    BytecodeUtils.invokeMethod(visitor, INVOKEINTERFACE, LIST$STREAM);

    // this::convert
    visitor.visitVarInsn(ALOAD, 0);
    visitor.visitInvokeDynamicInsn(
        FUNCTION$APPLY.getName(),
        "(L" + internalName + ";)" + Type.getDescriptor(Function.class),
        new Handle(H_INVOKESTATIC,
            Type.getInternalName(LambdaMetafactory.class),
            LAMBDA_META_FACTORY$METAFACOTRY.getName(),
            Type.getMethodDescriptor(LAMBDA_META_FACTORY$METAFACOTRY),
            false),
        Type.getType(ReflectUtils.getMethodDescriptor(Object.class, Object.class)),
        new Handle(H_INVOKEVIRTUAL,
            internalName,
            CONVERTER$CONVERT.getName(),
            ReflectUtils.getMethodDescriptor(tType, sType),
            false),
        Type.getType(ReflectUtils.getMethodDescriptor(tType, sType)));

    // stream.map()
    BytecodeUtils.invokeMethod(visitor, INVOKEINTERFACE, STREAM$MAP);

    // collector.toList()
    BytecodeUtils.invokeMethod(visitor, INVOKESTATIC, COLLECTORS$TO_LIST);

    // stream.collect()
    BytecodeUtils.invokeMethod(visitor, INVOKEINTERFACE, STREAM$COLLECT);
    visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(List.class));

    // target.setField()
    BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, setter);
    dropSetterReturnVal(visitor, setter);
  }

  /**
   * 判断字段是否为 null, 如果为 null 则跳转到 label
   * <p>if (source.getField() == null) { // skip};</p>
   * <pre>
   *   aload_1
   *   invokevirtual #Conveter getField()
   *   ifnull #label
   * </pre>
   *
   * @param visitor 方法编写器
   * @param getter  字段 getter
   * @return 用于跳转的 label
   */
  private Label skipFieldIfNull(MethodVisitor visitor, Method getter) {
    Label label = new Label();
    visitor.visitVarInsn(ALOAD, 1);
    BytecodeUtils.invokeMethod(visitor, INVOKEVIRTUAL, getter);
    visitor.visitJumpInsn(IFNULL, label);
    return label;
  }

  /**
   * 如果 setter 有返回值, 则丢弃掉
   *
   * @param visitor 方法编写器
   * @param setter  拷贝目标字段 setter 方法
   */
  private void dropSetterReturnVal(MethodVisitor visitor, Method setter) {
    if (!setter.getReturnType().equals(Void.TYPE)) {
      visitor.visitInsn(POP);
    }
  }

  private boolean isBoxing(Field sField, Field tField) {
    Class<?> sType = sField.getType();
    Class<?> tType = tField.getType();
    if (sType.equals(int.class)) {
      return tType == Integer.class;
    } else if (sType.equals(short.class)) {
      return tType == Short.class;
    } else if (sType.equals(long.class)) {
      return tType == Long.class;
    } else if (sType.equals(byte.class)) {
      return tType == Byte.class;
    } else if (sType.equals(char.class)) {
      return tType == Character.class;
    } else if (sType.equals(float.class)) {
      return tType == Float.class;
    } else if (sType.equals(double.class)) {
      return tType == Double.class;
    } else if (sType.equals(boolean.class)) {
      return tType == Boolean.class;
    }

    return false;
  }

  private boolean isUnboxing(Field sField, Field tField) {
    Class<?> sType = sField.getType();
    Class<?> tType = tField.getType();
    if (sType == Integer.class) {
      return tType.equals(int.class);
    } else if (sType == Short.class) {
      return tType.equals(short.class);
    } else if (sType == Long.class) {
      return tType.equals(long.class);
    } else if (sType == Byte.class) {
      return tType.equals(byte.class);
    } else if (sType == Character.class) {
      return tType.equals(char.class);
    } else if (sType == Float.class) {
      return tType.equals(float.class);
    } else if (sType == Double.class) {
      return tType.equals(double.class);
    } else if (sType == Boolean.class) {
      return tType.equals(boolean.class);
    }
    return false;
  }

  /**
   * 判断是否属于递归拷贝
   *
   * @param sType  拷贝来源类
   * @param sField 拷贝来源字段
   * @param tType  拷贝目标类
   * @param tField 拷贝目标字段
   * @return 是否属于递归拷贝
   */
  private boolean isRecursionCopy(Class<?> sType, Field sField, Class<?> tType, Field tField) {
    return sType == sField.getType() && tType == tField.getType();
  }

  /**
   * 判断类型是否兼容(包括范型)
   *
   * @param sField 拷贝来源字段
   * @param tField 拷贝目标字段
   * @return 类型是否相等
   */
  private boolean isTypeCompatible(Field sField, Field tField) {
    java.lang.reflect.Type sFieldType = sField.getGenericType();
    java.lang.reflect.Type tFieldType = tField.getGenericType();

    if (sFieldType instanceof Class) {
      // 如果不是范型类型, 直接判断两者的类的继承关系
      return tField.getType().isAssignableFrom(sField.getType());
    } else if (sFieldType.equals(tFieldType)) {
      // 如果两者类型完全一致(包括范型), 则确定兼容
      return true;
    } else if (sFieldType instanceof ParameterizedType && tFieldType instanceof ParameterizedType) {
      // 如果两者均为范型且不完全一致, 那么只有原始类型继承兼容并且范型一致才为兼容
      ParameterizedType sFieldParameterizedType = (ParameterizedType) sFieldType;
      ParameterizedType tFieldParameterizedType = (ParameterizedType) tFieldType;
      if (!((Class<?>) tFieldParameterizedType.getRawType())
              .isAssignableFrom((Class<?>) sFieldParameterizedType.getRawType())) {
        // 原始类型不一致, 不兼容
        return false;
      }
      if (sFieldParameterizedType.getActualTypeArguments().length
              != tFieldParameterizedType.getActualTypeArguments().length) {
        // 范型数量不一致, 不兼容
        return false;
      }
      for (int i = 0; i < sFieldParameterizedType.getActualTypeArguments().length; i++) {
        if (!tFieldParameterizedType.getActualTypeArguments()[i]
                .equals(sFieldParameterizedType.getActualTypeArguments()[i])) {
          // 第 i 个范型不一致, 不兼容
          return false;
        }
      }

      // 所有范型一致, 兼容
      return true;
    }

    // 其他均为不兼容
    return false;
  }

  /**
   * 获取类型处理器
   *
   * @param sField 拷贝来源字段
   * @param tField 拷贝目标字段
   * @return 类型处理器
   * @since 0.1.0
   */
  private Class<? extends TypeHandler<?, ?>> getTypeHandler(Field sField, Field tField) {
    Property property = tField.getAnnotation(Property.class);
    if (property == null) {
      return null;
    }

    java.lang.reflect.Type sType = sField.getGenericType();
    java.lang.reflect.Type tType = tField.getGenericType();
    for (Class<? extends TypeHandler<?, ?>> typeHandler : property.typeHandler()) {
      for (Method method : typeHandler.getDeclaredMethods()) {
        if (!method.getName().equals(TypeHandler.METHOD_NAME)) {
          continue;
        }
        if (!Modifier.isPublic(method.getModifiers())) {
          continue;
        }
        if (!method.getGenericReturnType().equals(tType)) {
          continue;
        }
        if (method.getGenericParameterTypes().length != 1 || !method.getGenericParameterTypes()[0].equals(sType)) {
          continue;
        }
        return typeHandler;
      }
    }

    return null;
  }

  /**
   * 判断是否是列表递归拷贝
   *
   * @param sType  拷贝来源类
   * @param sField 拷贝来源字段
   * @param tType  拷贝目标类
   * @param tField 拷贝目标字段
   * @return 是否是列表递归拷贝
   */
  private boolean isListRecursionCopy(Class<?> sType, Field sField, Class<?> tType, Field tField) {
    if (sField.getType() != List.class) {
      return false;
    }
    if (tField.getType() != List.class) {
      return false;
    }

    return getListElementType(sField) == sType && getListElementType(tField) == tType;
  }

  /**
   * 获取列表字段元素的范型
   *
   * @param field 字段
   * @return 列表字段元素范型
   */
  private Class<?> getListElementType(Field field) {
    return (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
  }

}