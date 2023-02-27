package io.github.tanyaofei.beancopier.core;

import io.github.tanyaofei.beancopier.ConverterFeatures;
import io.github.tanyaofei.beancopier.constants.InstantiateMode;
import io.github.tanyaofei.beancopier.converter.Converter;
import io.github.tanyaofei.beancopier.exception.CodeError;
import io.github.tanyaofei.beancopier.exception.DefineClassError;
import io.github.tanyaofei.beancopier.exception.InstantiationError;
import io.github.tanyaofei.beancopier.exception.VerifyException;
import io.github.tanyaofei.beancopier.utils.BytecodeUtils;
import io.github.tanyaofei.beancopier.utils.reflection.Reflections;
import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Factory used to generate {@link Converter} implementations
 *
 * @author tanyaofei
 * @since 0.0.1
 */
@Slf4j
public class ConverterFactory implements Opcodes {

  public final static Consumer<ConverterFeatures.Builder> DEFAULT_FEATURE = features -> {
  };

  /**
   * This ExecutorService is used to dump converter class
   */
  private final static ExecutorService debugWorkers = Executors.newSingleThreadExecutor();


  private final ConverterFeatures features;

  public ConverterFactory() {
    this(DEFAULT_FEATURE);
  }

  public ConverterFactory(
      @Nonnull Consumer<ConverterFeatures.Builder> features
  ) {
    var builder = ConverterFeatures.builder();
    features.accept(builder);
    this.features = builder.build();
  }

  /**
   * Use the given {@link java.lang.invoke.MethodHandles.Lookup} to define the converter class
   *
   * @param code java byte code
   * @return class
   */
  @Nonnull
  @SuppressWarnings("unchecked")
  private static <T> Class<T> defineClass(
      @Nonnull byte[] code,
      @Nonnull MethodHandles.Lookup lookup
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
  public <S, T> Converter<S, T> genConverter(
      @Nonnull Class<S> sourceType, @Nonnull Class<T> targetType
  ) {
    verifySourceType(sourceType);
    verifyTargetType(targetType);
    InstantiateMode newInstanceMode = getInstantiateMode(targetType);

    var lookup = Optional.ofNullable(features.getLookup()).orElse(Converter.LOOKUP);
    verifyPrivilege(sourceType, lookup);
    verifyPrivilege(targetType, lookup);

    var packageName = lookup.lookupClass().getPackageName();
    String className = packageName + "." + features
        .getNamingPolicy()
        .getSimpleClassName(sourceType, targetType);
    String internalName = Reflections.getInternalNameByClassName(className);
    byte[] code;
    var definition = new ConverterDefinition(
        internalName,
        sourceType,
        targetType,
        features,
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
      dumpBytecode(
          c == null
              ? code
              : BytecodeUtils.rename(code, c.getName().replace("/", "$")),
          c == null
              ? Reflections.getClassSimpleNameByInternalName(internalName) + "$" + Arrays.hashCode(code)
              : c.getSimpleName().replace("/", "$")
      );
    }

    try {
      return c.getConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new InstantiationError(c, e);
    }
  }

  private void dumpBytecode(byte[] code, String filename) {
    var directory = features.getDebugLocation();
    if (directory == null || directory.isBlank()) {
      return;
    }
    CompletableFuture.runAsync(() -> {
      try (var out = new FileOutputStream(directory + File.separatorChar + filename + ".class")) {
        out.write(code);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }, debugWorkers);
  }

  private void verifySourceType(Class<?> c) {
    Optional.ofNullable(Reflections.getDescriptionIfNotImportable(c)).ifPresent(desc -> {
      throw new VerifyException("Could not generate a converter that converts " + c.getName() + " because it's " + desc);
    });
  }

  private void verifyTargetType(Class<?> c) {
    Optional.ofNullable(Reflections.getDescriptionIfNotImportable(c)).ifPresent(desc -> {
      throw new VerifyException("Could not generate a converter that converts to " + c.getName() + " because it's " + desc);
    });

    Optional.ofNullable(Reflections.getDescriptionIfNotInstantiatable(c)).ifPresent(desc -> {
      throw new VerifyException("Could not generate a converter that converts to " + c.getName() + " because it's " + desc);
    });
  }

  private InstantiateMode getInstantiateMode(Class<?> c) {
    if (c.isRecord()) {
      return InstantiateMode.ALL_ARGS_CONSTRUCTOR;
    }

    if (features.isSkipNull()) {
      // if skipNull feature is set to true, required a no-args-constructor
      if (Reflections.hasPublicNoArgsConstructor(c)) {
        return InstantiateMode.NO_ARGS_CONSTRUCTOR;
      }
      throw new VerifyException("Could not generate a converter that converts to " + c.getName() + " because it hasn't a no-args-constructor(required by skipNull feature)");
    }

    if (Reflections.hasPublicAllArgsConstructor(c)) {
      return InstantiateMode.ALL_ARGS_CONSTRUCTOR;
    }

    if (Reflections.hasPublicNoArgsConstructor(c)) {
      return InstantiateMode.NO_ARGS_CONSTRUCTOR;
    }

    throw new VerifyException("Could not generate a converter that converts to " + c.getName() + " because it hasn't any available constructor(no-args-constructor or all-args-constructor");
  }

  public void verifyPrivilege(Class<?> c, MethodHandles.Lookup lookup) {
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