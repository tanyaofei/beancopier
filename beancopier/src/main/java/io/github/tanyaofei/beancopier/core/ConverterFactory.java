package io.github.tanyaofei.beancopier.core;

import io.github.tanyaofei.beancopier.ConverterConfiguration;
import io.github.tanyaofei.beancopier.constants.InstantiateMode;
import io.github.tanyaofei.beancopier.constants.Methods;
import io.github.tanyaofei.beancopier.converter.Converter;
import io.github.tanyaofei.beancopier.exception.ConverterGenerateException;
import io.github.tanyaofei.beancopier.exception.ConverterNewInstanceException;
import io.github.tanyaofei.beancopier.utils.BytecodeUtils;
import io.github.tanyaofei.beancopier.utils.StringUtils;
import io.github.tanyaofei.beancopier.utils.reflection.Reflections;
import org.jetbrains.annotations.Contract;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isPublic;

/**
 * Factory used to generate {@link Converter} implementations
 *
 * @author tanyaofei
 * @since 0.0.1
 */
public class ConverterFactory implements Opcodes, Methods {

  private final ConverterConfiguration configuration;

  public ConverterFactory(
      Consumer<ConverterConfiguration.Builder> consumer
  ) {
    var builder = ConverterConfiguration.builder();
    consumer.accept(builder);
    this.configuration = builder.build();
  }

  /**
   * Dump class bytecode to a class file on disk, never throw any exception
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
   * Use the specified Lookup to define the converter class
   *
   * @param code java byte code
   * @return class
   */
  @SuppressWarnings("unchecked")
  private static <T> Class<T> defineClass(
      byte[] code,
      MethodHandles.Lookup lookup
  ) throws IllegalAccessException {
    return (Class<T>) lookup.defineHiddenClass(code, true, MethodHandles.Lookup.ClassOption.NESTMATE).lookupClass();
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

    var lookup = Optional.ofNullable(configuration.getLookup()).orElse(Converter.LOOKUP);
    var packageName = lookup.lookupClass().getPackageName();
    String className = packageName + "." + configuration
        .getNamingPolicy()
        .getSimpleClassName(
            sourceType,
            targetType
        );
    String internalName = Reflections.getInternalNameByClassName(className);
    byte[] code;
    var definition = new ConverterDefinition(
        internalName,
        sourceType,
        targetType,
        configuration,
        newInstanceMode
    );
    try {
      code = new ConverterCodeWriter(definition).write();
    } catch (Throwable e) {
      throw new ConverterGenerateException(sourceType, targetType, e);
    }

    Class<Converter<S, T>> c = null;
    try {
      c = Objects.requireNonNull(defineClass(code, lookup));
    } catch (Throwable e) {
      throw new ConverterGenerateException(sourceType, targetType, e);
    } finally {
      String dumpPath = configuration.getClassDumpPath();
      if (StringUtils.hasLength(dumpPath)) {
        var hiddenClass = c;
        CompletableFuture.runAsync(() -> {
          byte[] dumpCode;
          String filename;
          if (hiddenClass == null) {
            dumpCode = code;
            filename = Reflections.getClassSimpleNameByInternalName(internalName);
          } else {
            dumpCode = BytecodeUtils.rename(code, hiddenClass.getName().replace("/", "$"));
            filename = hiddenClass.getSimpleName().replace("/", "$");
          }
          dumpClassSafely(dumpCode, dumpPath + File.separatorChar + filename + ".class");
        });
      }
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

    if (c.isRecord()) {
      return InstantiateMode.ALL_ARGS_CONSTRUCTOR;
    } else if (!configuration.isSkipNull() && Reflections.hasMatchedPublicAllArgsConstructor(c)) {
      return InstantiateMode.ALL_ARGS_CONSTRUCTOR;
    }

    if (Reflections.hasPublicNoArgsConstructor(c)) {
      return InstantiateMode.NO_ARGS_CONSTRUCTOR_THEN_GET_SET;
    }
    throw new ConverterGenerateException("'" + c.getName() + "' missing a public no-args-constructor or a public all-args-constructor");
  }

}