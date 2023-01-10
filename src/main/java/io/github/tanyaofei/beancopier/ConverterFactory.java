package io.github.tanyaofei.beancopier;

import com.google.common.reflect.TypeToken;
import io.github.tanyaofei.beancopier.annotation.Property;
import io.github.tanyaofei.beancopier.exception.ConverterGenerateException;
import io.github.tanyaofei.beancopier.exception.ConverterNewInstanceException;
import io.github.tanyaofei.beancopier.utils.Constants;
import io.github.tanyaofei.beancopier.utils.*;
import io.github.tanyaofei.beancopier.utils.Reflections.BeanProperty;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import sun.misc.Unsafe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static java.lang.reflect.Modifier.isPublic;

/**
 * 使用 ASM 字节码技术在运行时创建 Source 拷贝为 Target 的转换器字节码
 *
 * @author tanyaofei
 * @see DefaultClassLoader
 */
public class ConverterFactory implements Opcodes, MethodConstants {

  private final static Unsafe unsafe = UnsafeUtils.getUnsafe();

  /**
   * 字节码导出路径, 如果为 null 或者为空表示不导出
   */
  private final String classDumpPath;

  /**
   * 已使用的类名, 因为在生成过程中, 有可能出现重复的类名, 因此维护一个类名集来保证类名唯一
   */
  private final Set<String> reservedClassNames = new HashSet<>(64);

  /**
   * 类名生成策略
   *
   * @see NamingPolicy#getDefault()
   */
  private final NamingPolicy namingPolicy;

  /**
   * 类加载器, 生成的类将会通过此类加载器进行加载
   */
  private final ClassLoader classLoader;

  /**
   * 创建转换器工厂
   *
   * @param classLoader   类加载器, 如果为 null 则自动选取
   * @param namingPolicy  转换器类命名规则
   * @param classDumpPath 用于调试, 转换器类持久化到磁盘的路径, 如果为 null 或者为空则表示不持久化。如果路径不存在则自动创建文件详见
   */
  @Contract(pure = true)
  public ConverterFactory(
      @Nullable ClassLoader classLoader,
      @NotNull NamingPolicy namingPolicy,
      @Nullable String classDumpPath
  ) {
    this.classLoader = classLoader;
    this.classDumpPath = classDumpPath;
    this.namingPolicy = namingPolicy;
  }

  /**
   * 加载类, 在加载时会选用两个类加载器继承链更下级的类加载器来进行加载
   *
   * @param sc   拷贝来源类
   * @param tc   拷贝结果类
   * @param code 字节码
   * @return 类
   * @throws ConverterGenerateException 两个类没有继承关系, 则抛出磁异常
   */
  private static Class<?> chooseClassLoaderToDefineClass(Class<?> sc, Class<?> tc, byte[] code) {
    // 使用继承链中最下级的 classloader 加载, 这样这个 classloader 加载出来的类可以访问另外一个更上级 classloader 加载的类
    ClassLoader scl = sc.getClassLoader();
    ClassLoader tcl = tc.getClassLoader();
    ClassLoader classLoader = ClassLoaders.isAssignableFrom(scl, tcl)
        ? tcl
        : ClassLoaders.isAssignableFrom(tcl, scl)
        ? scl : null;

    if (classLoader == null) {
      throw new ConverterGenerateException(
          String.format("Converter can not access classes that loaded by unrelated classloaders in the same time (%s was loaded by '%s' but %s was loaded by '%s')",
              sc,
              sc.getClassLoader(),
              tc,
              tc.getClassLoader()
          ));
    }

    if (classLoader instanceof ConverterClassLoader) {
      return ((ConverterClassLoader) classLoader).defineClass(null, code);
    }

    return unsafe.defineClass(null, code, 0, code.length, classLoader, null);
  }

  /**
   * 创建 sc 拷贝为 tc 的转换器并加载到运行时内存中并创建实例
   *
   * @param sc  拷贝来源类
   * @param tc  拷贝目标类
   * @param <S> 拷贝来源
   * @param <T> 拷贝目标
   * @return sc  to  tc 转换器实例
   */
  @Contract(pure = true)
  @SuppressWarnings("unchecked")
  public <S, T> Converter<S, T> generateConverter(
      Class<S> sc, Class<T> tc
  ) {
    // 类型检查
    checkType(sc);
    checkType(tc);

    // 生成类名称
    String className;
    synchronized (reservedClassNames) {
      className = namingPolicy.getClassName(sc, tc, reservedClassNames::contains);
      reservedClassNames.add(className);
    }

    Class<Converter<S, T>> c;
    String internalName = CodeEmitter.classNameToInternalName(className);
    try {
      ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
      cw.visit(
          V1_8,
          ACC_PUBLIC | ACC_SYNTHETIC,
          internalName,
          CodeEmitter.getClassSignature(
              Constants.CLASS_INFO_OBJECT,
              ClassInfo.of(Converter.class, sc, tc)),
          Constants.INTERNAL_NAME_OBJECT,
          Constants.INTERNAL_NAME_ARRAY_CONVERTER
      );
      cw.visitSource(Constants.SOURCE_FILE, null);

      // 编写构造函数
      writeNoArgsConstructor(cw);
      // 编写 convert 实现方法
      if (writeConvertMethod(cw, internalName, sc, tc)) {
        // 编写 lambda 函数
        writeLambda$convert$0Method(cw, internalName, sc, tc);
      }
      // 编写 convert 抽象方法
      if (sc != Object.class || tc != Object.class) {
        writeConvertBridgeMethod(cw, internalName, sc, tc);
      }

      // 加载类
      final byte[] code = cw.toByteArray();
      dumpClassIfConfigured(code, Reflections.getClassSimpleNameByInternalName(internalName));

      if (classLoader == null) {
        c = (Class<Converter<S, T>>) chooseClassLoaderToDefineClass(sc, tc, code);
      } else if (classLoader instanceof ConverterClassLoader) {
        c = (Class<Converter<S, T>>) ((ConverterClassLoader) classLoader).defineClass(null, code);
      } else {
        c = (Class<Converter<S, T>>) unsafe.defineClass(null, code, 0, code.length, classLoader, null);
      }
    } catch (Exception e) {
      synchronized (reservedClassNames) {
        reservedClassNames.remove(className);
      }
      throw new ConverterGenerateException(sc, tc, e);
    }

    // 初始化对象
    try {
      return c.getConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new ConverterNewInstanceException(c, e);
    }
  }


  private void dumpClassIfConfigured(byte[] code, String filename) {
    if (StringUtils.hasNotLength(classDumpPath)) {
      return;
    }

    try (FileOutputStream out = new FileOutputStream(classDumpPath + File.separator + filename + ".class")) {
      out.write(code);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  /**
   * 编写无参构造方法
   * @param cw 类编写器
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
   * 编写 convert 的桥接方法
   *
   * @param cw           类编写器
   * @param internalName 转换器 JAVA 内部名称
   * @param sc        拷贝来源类
   * @param tc        拷贝目标类
   */
  private <S, T> void writeConvertBridgeMethod(
      ClassWriter cw, String internalName, Class<S> sc, Class<T> tc
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
   * @param sc 拷贝来源类
   * @param tc 拷贝目标类
   * @return 是否包含列表递归拷贝
   * @see Converter#convert(Object)
   */
  private <S, T> boolean writeConvertMethod(
      ClassWriter cw,
      String internalName,
      Class<S> sc,
      Class<T> tc
  ) {
    boolean hasListRecursionCopy = false;
    MethodVisitor v = cw.visitMethod(
        ACC_PUBLIC,
        CONVERTER$CONVERT.getName(),
        CodeEmitter.getMethodDescriptor(tc, sc),
        null,
        null
    );

    v.visitCode();

    // -- Target target = new Target()
    // new
    // dup
    // invokespecial #Converter <init>()V;
    // astore_2
    CodeEmitter.newInstanceViaNoArgsConstructor(v, tc);
    v.visitVarInsn(ASTORE, 2);
    // -- end

    Label jumpHere = null;
    Iterable<BeanProperty> setters = Reflections.getBeanSetters(tc);
    Map<String, BeanProperty> getters = BeanProperty.mapIterable(Reflections.getBeanGetters(sc));

    for (BeanProperty tbp : setters) {
      Field tf = tbp.getField();
      Property property = Optional.ofNullable(tf.getAnnotation(Property.class)).orElse(Constants.DEFAULT_PROPERTY);
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

      if (isRecursionCopy(sc, sf, tc, tf)) {
        // 单个递归
        if (jumpHere != null) {
          v.visitLabel(jumpHere);
        }
        jumpHere = skipFieldIfNull(v, getter);
        recursionCopy(v, internalName, sc, getter, tc, setter);
      } else if (isListRecursionCopy(sc, sf, tc, tf)) {
        // 列表递归
        if (jumpHere != null) {
          v.visitLabel(jumpHere);
        }
        jumpHere = skipFieldIfNull(v, getter);
        listRecursionCopy(v, internalName, sc, getter, tc, setter);
        hasListRecursionCopy = true;
      } else if (isCompatible(sf, tf)) {
        // 正常字段
        if (jumpHere != null) {
          v.visitLabel(jumpHere);
          jumpHere = null;
        }
        copy(v, getter, setter);
      }
    }

    // return target
    if (jumpHere != null) {
      v.visitLabel(jumpHere);
    }
    v.visitVarInsn(ALOAD, 2);
    v.visitInsn(ARETURN);
    v.visitMaxs(-1, -1);
    v.visitEnd();
    return hasListRecursionCopy;
  }

  private void writeLambda$convert$0Method(
      ClassWriter cw,
      String internalName,
      Class<?> sc,
      Class<?> tc
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
   * 拷贝普通字段
   *
   * @param v      方法编写器
   * @param getter 拷贝来源字段 getter 方法
   * @param setter 拷贝目标字段 setter
   */
  private void copy(
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
   * @param v      方法编写器
   * @param internalName 所属类 internalName
   * @param sc        拷贝来源类
   * @param getter       拷贝来源字段 getter 方法
   * @param tc        拷贝目标类
   * @param setter       拷贝目标字段 setter 方法
   */
  private void recursionCopy(
      MethodVisitor v,
      String internalName,
      Class<?> sc, Method getter,
      Class<?> tc, Method setter
  ) {
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
   * 编写拷贝递归字段(List)的 Code
   * <p>target.setField(source.getField().stream().map(this::convert).collect(Collectors.toList()))</p>
   *
   * @param v      方法编写器
   * @param internalName 所属类 internalName
   * @param sc        拷贝来源类
   * @param getter       拷贝来源字段 getter 方法
   * @param tc        拷贝目标类
   * @param setter       拷贝目标字段 setter 方法
   */
  private void listRecursionCopy(
      MethodVisitor v,
      String internalName,
      Class<?> sc, Method getter,
      Class<?> tc, Method setter
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
    CodeEmitter.invokeMethod(visitor, INVOKEVIRTUAL, getter);
    visitor.visitJumpInsn(IFNULL, label);
    return label;
  }

  /**
   * 如果 setter 有返回值, 则丢弃掉
   *
   * @param v 方法编写器
   * @param setter  拷贝目标字段 setter 方法
   */
  private void dropSetterReturnVal(MethodVisitor v, Method setter) {
    if (!setter.getReturnType().equals(Void.TYPE)) {
      v.visitInsn(POP);
    }
  }

  /**
   * 判断是否属于递归拷贝
   *
   * @param st  拷贝来源类
   * @param sf 拷贝来源字段
   * @param tc  拷贝目标类
   * @param tf 拷贝目标字段
   * @return 是否属于递归拷贝
   */
  private boolean isRecursionCopy(Class<?> st, Field sf, Class<?> tc, Field tf) {
    return st == sf.getType() && tc == tf.getType();
  }

  /**
   * 判断类型是否兼容(包括范型)
   *
   * @param sf 拷贝来源字段
   * @param tf 拷贝目标字段
   * @return 类型是否相等
   */
  private boolean isCompatible(Field sf, Field tf) {
    return TypeToken.of(sf.getGenericType()).isSubtypeOf(tf.getGenericType());

  }

  /**
   * 判断是否是列表递归拷贝
   *
   * @param sc  拷贝来源类
   * @param sf 拷贝来源字段
   * @param tc  拷贝目标类
   * @param tf 拷贝目标字段
   * @return 是否是列表递归拷贝
   */
  private boolean isListRecursionCopy(Class<?> sc, Field sf, Class<?> tc, Field tf) {
    if (!List.class.isAssignableFrom(sf.getType())) {
      return false;
    }
    if (!List.class.isAssignableFrom(tf.getType())) {
      return false;
    }
    return getListElementType(sf) == sc && getListElementType(tf) == tc;
  }

  /**
   * 获取列表字段元素的范型
   *
   * @param f 字段
   * @return 列表字段元素范型
   */
  @SuppressWarnings("unchecked")
  private Class<?> getListElementType(Field f) {
    return ((TypeToken<? extends List<?>>) TypeToken.of(f.getGenericType()))
        .getSupertype(List.class)
        .resolveType(List.class.getTypeParameters()[0])
        .getRawType();
  }

  private void checkType(Class<?> c) {
    if (!isPublic(c.getModifiers())) {
      throw new ConverterGenerateException("Class '" + c.getName() + "' is not public");
    }

    try {
      if (!isPublic(c.getConstructor().getModifiers())) {
        throw new ConverterGenerateException("Class '" + c.getName() + "' non-args-constructor is not public");
      }
    } catch (NoSuchMethodException e) {
      throw new ConverterGenerateException("Class '" + c.getName() + "' missing a non-args-constructor", e);
    }

  }

}