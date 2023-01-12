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
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isPublic;

/**
 * 使用 ASM 字节码技术在运行时创建 Source 拷贝为 Target 的转换器字节码
 *
 * @author tanyaofei
 * @see DefaultClassLoader
 * @since 0.0.1
 */
class ConverterFactory implements Opcodes, MethodConstants {

  private final static Unsafe unsafe = UnsafeUtils.getUnsafe();

  /**
   * 每个 classloader 已经注册的类名
   */
  private final static WeakHashMap<ClassLoader, Set<String>> reservedClassNames = new WeakHashMap<>(4);

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

  private static ClassLoader chooseClassLoader(Class<?> sc, Class<?> tc) {
    ClassLoader scl = sc.getClassLoader();
    ClassLoader tcl = tc.getClassLoader();

    ClassLoader cl;
    if (scl == null && tcl == null) {
      cl = ClassLoader.getSystemClassLoader();
    } else {
      cl = Reflections.isCLAssignableFrom(scl, tcl)
          ? tcl
          : Reflections.isCLAssignableFrom(tcl, scl)
          ? scl : null;
    }


    if (cl == null) {
      throw new ConverterGenerateException(
          String.format("Converter can not access classes that loaded by unrelated classloaders in the same time (%s was loaded by '%s' but %s was loaded by '%s')",
              sc,
              sc.getClassLoader(),
              tc,
              tc.getClassLoader()
          ));
    }
    return cl;
  }

  private static void dumpClass(byte[] code, String filename) {
    try (FileOutputStream out = new FileOutputStream(filename)) {
      out.write(code);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static Set<String> getReservedClassNames(ClassLoader cl) {
    Set<String> names = reservedClassNames.get(cl);
    if (names == null) {
      synchronized (reservedClassNames) {
        names = reservedClassNames.computeIfAbsent(cl, k -> new HashSet<>(32));
      }
    }
    return names;
  }

  /**
   * 创建 sc 拷贝为 tc 的转换器并加载到运行时内存中并创建实例
   *
   * @param sc  拷贝来源类
   * @param tc  拷贝目标类
   * @param <S> 拷贝来源
   * @param <T> 拷贝目标
   * @return 将来源拷贝到目标的转换器
   * @throws ConverterGenerateException    生成转换器字节码时发生异常
   * @throws ConverterNewInstanceException 初始化转换器发生异常
   */
  @Contract(pure = true)
  @SuppressWarnings("unchecked")
  public <S, T> Converter<S, T> generateConverter(
      Class<S> sc, Class<T> tc
  ) {
    // 类型检查
    checkSourceType(sc);
    checkTargetType(tc);

    ClassLoader cl = Optional.ofNullable(configuration.getClassLoader()).orElse(chooseClassLoader(sc, tc));
    // 生成类名称
    String className;
    Set<String> classNames = getReservedClassNames(cl);
    synchronized (classNames) {
      className = configuration.getNamingPolicy().getClassName(sc, tc, classNames::contains);
      classNames.add(className);
    }

    Class<Converter<S, T>> c;
    String internalName = CodeEmitter.classNameToInternalName(className);
    try {
      final byte[] code = new ConverterCodeWriter(internalName, sc, tc, configuration).write();
      if (StringUtils.hasLength(configuration.getClassDumpPath())) {
        dumpClass(code, configuration.getClassDumpPath() + File.separatorChar + Reflections.getClassSimpleNameByInternalName(internalName) + ".class");
      }

      if (cl instanceof ConverterClassLoader) {
        c = (Class<Converter<S, T>>) ((ConverterClassLoader) cl).defineClass(null, code);
      } else {
        c = (Class<Converter<S, T>>) unsafe.defineClass(null, code, 0, code.length, cl, null);
      }
    } catch (Exception e) {
      synchronized (classNames) {
        classNames.remove(className);
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