package io.github.tanyaofei.beancopier.core;

import io.github.tanyaofei.beancopier.ConverterConfiguration;
import io.github.tanyaofei.beancopier.constants.InstantiateMode;
import io.github.tanyaofei.beancopier.converter.Converter;
import io.github.tanyaofei.beancopier.exception.CodeError;
import io.github.tanyaofei.beancopier.exception.DefineClassError;
import io.github.tanyaofei.beancopier.exception.InstantiationError;
import io.github.tanyaofei.beancopier.exception.VerifyException;
import io.github.tanyaofei.beancopier.utils.BytecodeUtils;
import io.github.tanyaofei.beancopier.utils.StringUtils;
import io.github.tanyaofei.beancopier.utils.reflection.Reflections;
import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
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
@Slf4j
public class ConverterFactory implements Opcodes {

  private final ConverterConfiguration configuration;

  public ConverterFactory(
      Consumer<ConverterConfiguration.Builder> config
  ) {
    var builder = ConverterConfiguration.builder();
    config.accept(builder);
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
      log.info("Converter class was dumped at {}", filename);
    } catch (Throwable e) {
      log.warn("Failed to dump converter class", e);
    }
  }

  /**
   * Use the specified {@link java.lang.invoke.MethodHandles.Lookup} to define the converter class
   *
   * @param code java byte code
   * @return class
   */
  @SuppressWarnings("unchecked")
  private static <T> Class<T> defineClass(
      byte[] code,
      MethodHandles.Lookup lookup
  ) throws IllegalAccessException {
    return (Class<T>) lookup.defineHiddenClass(code, true).lookupClass();
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
  @Nonnull
  public <S, T> Converter<S, T> generateConverter(
      @Nonnull Class<S> sourceType, @Nonnull Class<T> targetType
  ) {
    checkSourceType(sourceType);
    var newInstanceMode = checkTargetType(targetType);

    var lookup = Optional.ofNullable(configuration.getLookup()).orElse(Converter.LOOKUP);
    checkPrivilege(sourceType, lookup);
    checkPrivilege(targetType, lookup);

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
      throw new CodeError(sourceType, targetType, e);
    }

    Class<Converter<S, T>> c = null;
    try {
      c = Objects.requireNonNull(defineClass(code, lookup));
    } catch (Throwable e) {
      throw new DefineClassError(code, sourceType, targetType, e);
    } finally {
      String dumpPath = configuration.getClassDumpPath();
      if (StringUtils.hasLength(dumpPath)) {
        var hiddenClass = c;
        CompletableFuture.runAsync(() -> {
          byte[] dumpCode;
          String filename;
          if (hiddenClass == null) {
            // define class failed
            dumpCode = code;
            filename = Reflections.getClassSimpleNameByInternalName(internalName);
          } else {
            // define class succeed
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
      throw new InstantiationError(c, e);
    }
  }

  /**
   * Check the specified can be import from outer class. If not, a {@link VerifyException} will be thrown
   *
   * @param c the specified class
   */
  private void checkImportable(Class<?> c) {
    if (c.isAnonymousClass()) {
      throw new VerifyException("Can not import a anonymous class: " + c.getName());
    }

    if (c.isLocalClass()) {
      throw new VerifyException("Can not import a local class: " + c.getName());
    }

    if (!isPublic(c.getModifiers())) {
      throw new VerifyException("Can not import a " + Modifier.toString(c.getModifiers()) + " class: " + c.getName());
    }
  }

  private void checkSourceType(Class<?> c) {
    checkImportable(c);
  }

  private InstantiateMode checkTargetType(Class<?> c) {
    checkImportable(c);

    if (c.isInterface()) {
      throw new VerifyException("Can not instantiate an interface: " + c.getName());
    }

    if (isAbstract(c.getModifiers())) {
      throw new VerifyException("Can not instantiate an abstract class: " + c.getName());
    }

    if (c.isEnum()) {
      throw new VerifyException("Can not instantiate an enum class: " + c.getName());
    }

    if (!Reflections.isEnclosingClass(c)) {
      throw new VerifyException("Can not instantiate an enclosing class: " + c.getName());
    }

    if (c.isRecord()) {
      return InstantiateMode.ALL_ARGS_CONSTRUCTOR;
    } else if (!configuration.isSkipNull() && Reflections.hasPublicAllArgsConstructor(c)) {
      return InstantiateMode.ALL_ARGS_CONSTRUCTOR;
    }

    if (Reflections.hasPublicNoArgsConstructor(c)) {
      return InstantiateMode.NO_ARGS_CONSTRUCTOR_THEN_SET;
    }
    throw new VerifyException(
        "Can not instantiate the class without public no-args-constructor or all-args-constructor: " + c.getName()
    );
  }

  public void checkPrivilege(Class<?> c, MethodHandles.Lookup lookup) {
    try {
      lookup.findClass(c.getName());
    } catch (IllegalAccessException e) {
      String message = (lookup == Converter.LOOKUP
                        ? "This BeanCopierImpl instance can not access class: " + c.getName()
                        : "The lookup defined in configuration can not access class: " + c.getName())
          + ", use `new BeanCopierImpl(config -> config.lookup(A_LOOKUP_THAT_HAS_PRIVILEGE_ACCESS))` instead";
      throw new VerifyException(message, e);
    } catch (ClassNotFoundException e) {
      throw new VerifyException(
          c + "(loaded by " + c.getClassLoader() + ") is not visible to this BeanCopier instance with classloader: "
              + lookup.lookupClass().getClassLoader()
              + ", use `new BeanCopierImpl(config -> config.lookup(A_LOOKUP_CAN_VISIT_THE_CLASS))` instead",
          e
      );
    }
  }

}