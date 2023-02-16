package io.github.tanyaofei.beancopier.core;

import io.github.tanyaofei.beancopier.ConverterClassLoader;
import io.github.tanyaofei.beancopier.ConverterConfiguration;
import io.github.tanyaofei.beancopier.constants.InstantiateMode;
import io.github.tanyaofei.beancopier.constants.Methods;
import io.github.tanyaofei.beancopier.converter.Converter;
import io.github.tanyaofei.beancopier.exception.ConverterGenerateException;
import io.github.tanyaofei.beancopier.exception.ConverterNewInstanceException;
import io.github.tanyaofei.beancopier.utils.StringUtils;
import io.github.tanyaofei.beancopier.utils.reflection.Reflections;
import lombok.var;
import org.jetbrains.annotations.Contract;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isPublic;

/**
 * Factory used to generate {@link Converter} Implementations
 *
 * @author tanyaofei
 * @see DefaultClassLoader
 * @since 0.0.1
 */
public class ConverterFactory implements Opcodes, Methods {

  /**
   * The defineClass method handle of {@link ClassLoader}
   */
  private static final MethodHandle defineClass;

  /**
   * The classnames register in each classloader
   */
  private final static WeakHashMap<ClassLoader, Set<String>> classLoaderReservedClassNames = new WeakHashMap<>(4);

  // breakup module protection and initial "defineClass" field
  static {
    try {
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

  /**
   * detect which classloader is at lower level, an exception will be thrown if the tow classloader have none relationship
   *
   * @param sourceType the class of source
   * @param targetType the class of target
   * @return a classloader will be used to define class
   */
  private static ClassLoader chooseClassLoader(Class<?> sourceType, Class<?> targetType) {
    var scl = sourceType.getClassLoader();
    var tcl = targetType.getClassLoader();

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
          String.format(
              "Converter can not access classes that loaded by unrelated classloaders in the same time (%s was loaded by '%s' but %s was loaded by '%s')",
              sourceType,
              sourceType.getClassLoader(),
              targetType,
              targetType.getClassLoader()
          ));
    }
    return cl;
  }

  /**
   * dump class bytecode to a class file on disk, never throw any exception
   *
   * @param code     java byte code
   * @param filename the filename will be created, end of '.class' usually
   */
  private static void dumpClassSafely(byte[] code, String filename) {
    try (var out = new FileOutputStream(filename)) {
      out.write(code);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  /**
   * define a class via specified classloader
   *
   * @param code JAVA byte code
   * @param cl   the specified classloader used to define a class
   * @return 类
   */
  @SuppressWarnings("unchecked")
  private static <T> Class<T> defineClass(byte[] code, ClassLoader cl) {
    if (cl instanceof ConverterClassLoader) {
      var ccl = (ConverterClassLoader) cl;
      return (Class<T>) ccl.defineClass(null, code);
    }

    try {
      return (Class<T>) defineClass.invoke(cl, (String) null, code, 0, code.length);
    } catch (Throwable e) {
      throw new ConverterNewInstanceException("Failed to define class", e);
    }
  }

  /**
   * Create a converter that has ability to new instance a target and copy source to it
   *
   * @param sourceType the class of source
   * @param targetType the class of target
   * @param <S>        the type of source
   * @param <T>        the type of target
   * @return A converter that has ability to copy
   */
  @Contract(pure = true)
  public <S, T> Converter<S, T> generateConverter(
      Class<S> sourceType, Class<T> targetType
  ) {
    checkSourceType(sourceType);
    var newInstanceMode = checkTargetType(targetType);

    var cl = Optional.ofNullable(configuration.getClassLoader()).orElse(chooseClassLoader(sourceType, targetType));
    String className;
    var reservedClassnames = getReservedClassNames(cl);
    synchronized (reservedClassnames) {
      className = configuration.getNamingPolicy().getClassName(sourceType, targetType, reservedClassnames::contains);
      reservedClassnames.add(className);
    }

    Class<Converter<S, T>> c;
    String internalName = Reflections.getInternalNameByClassName(className);
    try {
      final var code = new ConverterCodeWriter(new ConverterDefinition(
          internalName,
          sourceType,
          targetType,
          configuration,
          newInstanceMode
      )).write();
      if (StringUtils.hasLength(configuration.getClassDumpPath())) {
        dumpClassSafely(
            code,
            configuration.getClassDumpPath() + File.separatorChar + Reflections.getClassSimpleNameByInternalName(
                internalName) + ".class"
        );
      }
      c = defineClass(code, cl);
    } catch (Exception e) {
      synchronized (reservedClassnames) {
        reservedClassnames.remove(className);
      }
      throw new ConverterGenerateException(sourceType, targetType, e);
    }

    try {
      return c.getConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new ConverterNewInstanceException(c, e);
    }
  }

  private void checkSourceType(Class<?> c) {
    int modifiers = c.getModifiers();

    // can not import
    if (c.isLocalClass()) {
      throw new ConverterGenerateException("'" + c.getName() + "' is a local class");
    }

    // can not import
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

  private InstantiateMode checkTargetType(Class<?> c) {
    int modifiers = c.getModifiers();

    // can not import
    if (c.isLocalClass()) {
      throw new ConverterGenerateException("'" + c.getName() + "' is a local class");
    }

    // can not import
    if (!isPublic(modifiers)) {
      throw new ConverterGenerateException("'" + c.getName() + "' is not a public class");
    }

    // can not new instance
    if (c.isInterface()) {
      throw new ConverterGenerateException("'" + c.getName() + "' is an interface");
    }

    // can not new instance
    if (isAbstract(modifiers)) {
      throw new ConverterGenerateException("'" + c.getName() + "' is an abstract class");
    }

    // can not new instance
    if (c.isEnum()) {
      throw new ConverterGenerateException("'" + c.getName() + "' is an enum class");
    }

    // can not new instance
    if (!Reflections.isEnclosingClass(c)) {
      throw new ConverterGenerateException("'" + c.getName() + "' is not a enclosing class");
    }

    if (Reflections.hasMatchedPublicAllArgsConstructor(c)) {
      return InstantiateMode.ALL_ARGS_CONSTRUCTOR;
    }
    if (Reflections.hasPublicNoArgsConstructor(c)) {
      return InstantiateMode.NO_ARGS_CONSTRUCTOR_THEN_GET_SET;
    }
    throw new ConverterGenerateException("'" + c.getName() + "' missing a public no-args-constructor or a public all-args-constructor");
  }

}