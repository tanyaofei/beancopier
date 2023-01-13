package io.github.tanyaofei.beancopier.core;

import io.github.tanyaofei.beancopier.ConverterClassLoader;
import io.github.tanyaofei.beancopier.ConverterConfiguration;
import io.github.tanyaofei.beancopier.constants.Methods;
import io.github.tanyaofei.beancopier.constants.NewInstanceMode;
import io.github.tanyaofei.beancopier.converter.Converter;
import io.github.tanyaofei.beancopier.exception.ConverterGenerateException;
import io.github.tanyaofei.beancopier.exception.ConverterNewInstanceException;
import io.github.tanyaofei.beancopier.utils.BytecodeUtils;
import io.github.tanyaofei.beancopier.utils.StringUtils;
import io.github.tanyaofei.beancopier.utils.breakout.HackerClassLoader;
import io.github.tanyaofei.beancopier.utils.breakout.Injector;
import io.github.tanyaofei.beancopier.utils.reflection.Reflections;
import org.jetbrains.annotations.Contract;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
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
public class ConverterFactory implements Opcodes, Methods {

  private static final MethodHandle defineClass;

  /**
   * 每个 classloader 已经注册的类名
   */
  private final static WeakHashMap<ClassLoader, Set<String>> classLoaderReservedClassNames = new WeakHashMap<>(4);

  /**
   * JDK 模块权限越狱
   */
  @SuppressWarnings("all")
  private static void breakupJDK9ModuleProtection() throws ClassNotFoundException {
    var cl = new HackerClassLoader();
    var proxy = Proxy.newProxyInstance(cl, new Class[]{Class.forName("jdk.internal.access.JavaLangAccess")}, (proxy0, method, args) -> null);
    var injectorClass = cl.define(BytecodeUtils.repackage(Injector.class, "com.sun.proxy.jdk.proxy3"));
    Class.forName(injectorClass.getName(), true, cl);
  }

  static {
    try {
      breakupJDK9ModuleProtection();
      var field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
      field.setAccessible(true);
      var truestedLookup = (MethodHandles.Lookup) field.get(MethodHandles.Lookup.class);
      defineClass = truestedLookup.findSpecial(
          ClassLoader.class,
          "defineClass",
          MethodType.methodType(Class.class, String.class, byte[].class, int.class, int.class),
          ClassLoader.class
      );
    } catch (Throwable e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private final ConverterConfiguration configuration;

  public ConverterFactory(
      Consumer<ConverterConfiguration.Builder> consumer
  ) {
    var builder = ConverterConfiguration.builder();
    consumer.accept(builder);
    this.configuration = builder.build();
  }

  private static ClassLoader chooseClassLoader(Class<?> sc, Class<?> tc) {
    var scl = sc.getClassLoader();
    var tcl = tc.getClassLoader();

    ClassLoader cl;
    if (scl == null && tcl == null) {
      cl = ClassLoader.getSystemClassLoader();
    } else {
      cl = Reflections.isClAssignableFrom(scl, tcl)
          ? tcl
          : Reflections.isClAssignableFrom(tcl, scl)
            ? scl
            : null;
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

  /**
   * 持久化字节码到磁盘, 不会抛出任何异常
   *
   * @param code     字节码
   * @param filename 文件名，通常以 .class 结尾
   */
  private static void dumpClassSafely(byte[] code, String filename) {
    try (var out = new FileOutputStream(filename)) {
      out.write(code);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  /**
   * 使用特定的 classloader 加载字节码
   *
   * @param code 字节码
   * @param cl   使用这个 classloader 进行加载
   * @return 类
   */
  private static Class<?> defineClass(byte[] code, ClassLoader cl) {
    if (cl instanceof ConverterClassLoader ccl) {
      return ccl.defineClass(null, code);
    }

    try {
      return (Class<?>) defineClass.invoke(cl, (String) null, code, 0, code.length);
    } catch (Throwable e) {
      throw new ConverterNewInstanceException("Failed to define class", e);
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
  @SuppressWarnings({"unchecked"})
  public <S, T> Converter<S, T> generateConverter(
      Class<S> sc, Class<T> tc
  ) {
    // 类型检查
    checkSourceType(sc);
    var newInstanceMode = checkTargetType(tc);

    var cl = Optional.ofNullable(configuration.getClassLoader()).orElse(chooseClassLoader(sc, tc));
    // 生成类名称
    String className;
    var reservedClassnames = getReservedClassNames(cl);
    synchronized (reservedClassnames) {
      className = configuration.getNamingPolicy().getClassName(sc, tc, reservedClassnames::contains);
      reservedClassnames.add(className);
    }

    Class<Converter<S, T>> c;
    String internalName = Reflections.getInternalNameByClassName(className);
    try {
      final var code = new ConverterCodeWriter(new ConverterDefinition(internalName, sc, tc, configuration, newInstanceMode)).write();
      if (StringUtils.hasLength(configuration.getClassDumpPath())) {
        dumpClassSafely(code, configuration.getClassDumpPath() + File.separatorChar + Reflections.getClassSimpleNameByInternalName(internalName) + ".class");
      }
      c = (Class<Converter<S, T>>) defineClass(code, cl);
    } catch (Exception e) {
      synchronized (reservedClassnames) {
        reservedClassnames.remove(className);
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

  private static Set<String> getReservedClassNames(ClassLoader cl) {
    var names = classLoaderReservedClassNames.get(cl);
    if (names == null) {
      synchronized (classLoaderReservedClassNames) {
        names = new HashSet<>(32);
        classLoaderReservedClassNames.put(cl, names);
      }
    }
    return names;
  }

  private NewInstanceMode checkTargetType(Class<?> c) {
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

    if (Reflections.hasMatchedPublicAllArgsConstructor(c)) {
      return NewInstanceMode.ALL_ARGS_CONSTRUCTOR;
    }
    if (Reflections.hasPublicNoArgsConstructor(c)) {
      return NewInstanceMode.NO_ARGS_CONSTRUCTOR_THEN_GET_SET;
    }
    throw new ConverterGenerateException("'" + c.getName() + "' missing a public no-args-constructor or a public all-args-constructor");
  }

}