package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.exception.ConverterGenerateException;
import io.github.tanyaofei.beancopier.exception.ConverterNewInstanceException;
import io.github.tanyaofei.beancopier.utils.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import sun.misc.Unsafe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isPublic;

/**
 * 使用 ASM 字节码技术在运行时创建 Source 拷贝为 Target 的转换器字节码
 *
 * @author tanyaofei
 * @see DefaultClassLoader
 */
class ConverterFactory implements Opcodes, MethodConstants {

  private final static Unsafe unsafe = UnsafeUtils.getUnsafe();

  /**
   * 已使用的类名, 因为在生成过程中, 有可能出现重复的类名, 因此维护一个类名集来保证类名唯一
   */
  private final Set<String> reservedClassNames = new HashSet<>(64);

  private final ConverterConfiguration configuration;

  /**
   * 创建转换器工厂
   *
   * @param classLoader   类加载器, 如果为 null 则自动选取
   * @param namingPolicy  转换器类命名规则
   * @param classDumpPath 用于调试, 转换器类持久化到磁盘的路径, 如果为 null 或者为空则表示不持久化。如果路径不存在则自动创建文件详见
   * @deprecated 0.2.0 将会移除此方法, 请使用 {@link #ConverterFactory(Consumer)}
   */
  @Deprecated
  @Contract(pure = true)
  public ConverterFactory(
      @Nullable ClassLoader classLoader,
      @NotNull NamingPolicy namingPolicy,
      @Nullable String classDumpPath
  ) {
    this(builder -> builder.classLoader(classLoader).namingPolicy(namingPolicy).classDumpPath(classDumpPath));
  }

  public ConverterFactory(
      Consumer<ConverterConfiguration.ConverterConfigurationBuilder> consumer
  ) {
    ConverterConfiguration.ConverterConfigurationBuilder builder = ConverterConfiguration.builder();
    consumer.accept(builder);
    this.configuration = builder.build();
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
    ClassLoader cl = Reflections.isCLAssignableFrom(scl, tcl)
        ? tcl
        : Reflections.isCLAssignableFrom(tcl, scl)
        ? scl : null;

    if (cl == null) {
      throw new ConverterGenerateException(
          String.format("Converter can not access classes that loaded by unrelated classloaders in the same time (%s was loaded by '%s' but %s was loaded by '%s')",
              sc,
              sc.getClassLoader(),
              tc,
              tc.getClassLoader()
          ));
    }

    if (cl instanceof ConverterClassLoader) {
      return ((ConverterClassLoader) cl).defineClass(null, code);
    }

    return unsafe.defineClass(null, code, 0, code.length, cl, null);
  }

  private static void dumpClass(byte[] code, String filename) {
    try (FileOutputStream out = new FileOutputStream(filename)) {
      out.write(code);
    } catch (IOException e) {
      e.printStackTrace();
    }
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
    checkSourceType(sc);
    checkTargetType(tc);
    if (!Reflections.isEnclosingClass(tc)) {
      throw new ConverterGenerateException("'" + tc.getName() + "' is not a enclosing class");
    }
    if (!Reflections.hasPublicNoArgsConstructor(tc)) {
      throw new ConverterGenerateException("'" + tc.getName() + "' missing a public no-args-constructor");
    }

    // 生成类名称
    String className;
    synchronized (reservedClassNames) {
      className = configuration.getNamingPolicy().getClassName(sc, tc, reservedClassNames::contains);
      reservedClassNames.add(className);
    }

    Class<Converter<S, T>> c;
    String internalName = CodeEmitter.classNameToInternalName(className);
    try {
      final byte[] code = new ConverterCodeWriter(internalName, sc, tc, configuration).write();
      if (StringUtils.hasLength(configuration.getClassDumpPath())) {
        dumpClass(code, configuration.getClassDumpPath() + File.separatorChar + Reflections.getClassSimpleNameByInternalName(internalName) + ".class");
      }

      ClassLoader classLoader = configuration.getClassLoader();
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

  private void checkSourceType(Class<?> c) {
    int modifiers = c.getModifiers();

    // 无法 import
    if (c.isLocalClass()) {
      throw new ConverterGenerateException("'" + c.getName() + "' is a local class");
    }

    // 无法 import
    if (!isPublic(modifiers)) {
      throw new ConverterGenerateException("'" + c.getName() + "' is not a public class");
    }

  }

  private void checkTargetType(Class<?> c) {
    int modifiers = c.getModifiers();

    // 无法 import
    if (c.isLocalClass()) {
      throw new ConverterGenerateException("'" + c.getName() + "' is a local class");
    }

    // 无法 import
    if (!isPublic(modifiers)) {
      throw new ConverterGenerateException("'" + c.getName() + "' is not a public class");
    }

    // 接口无法实例化
    if (c.isInterface()) {
      throw new ConverterGenerateException("'" + c.getName() + "' is an interface");
    }

    // 抽象类无法实例化
    if (isAbstract(modifiers)) {
      throw new ConverterGenerateException("'" + c.getName() + "' is an abstract class");
    }

    // 枚举不能实例化
    if (c.isEnum()) {
      throw new ConverterGenerateException("'" + c.getName() + "' is an enum class");
    }

    // 非封闭类无法实例化
    if (!Reflections.isEnclosingClass(c)) {
      throw new ConverterGenerateException("'" + c.getName() + "' is not a enclosing class");
    }

    // 没有无参构造方法无法实例化
    if (!Reflections.hasPublicNoArgsConstructor(c)) {
      throw new ConverterGenerateException("'" + c.getName() + "' missing the public no-args-constructor");
    }
  }

}