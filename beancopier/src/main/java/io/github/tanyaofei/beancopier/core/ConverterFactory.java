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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
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
              ? Reflections.getClassSimpleNameByInternalName(internalName) + "$" + Arrays.hashCode(code) + ".class"
              : c.getSimpleName().replace("/", "$") + ".class"
      );
    }

    try {
      return c.getConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new InstantiationError(c, e);
    }
  }

  /**
   * Dump bytecode if to {@code features.getDebugLocation()} is not {@code null} or empty.
   * This method run in a background thread, it won't affect caller's thread even any exception was thrown
   *
   * @param code     bytecode
   * @param filename filename
   */
  private void dumpBytecode(byte[] code, String filename) {
    var directory = features.getDebugLocation();
    if (directory == null || directory.isBlank()) {
      return;
    }
    var file = FileSystems.getDefault().getPath(directory, filename).normalize().toFile();
    CompletableFuture.runAsync(() -> {
      try (var out = new FileOutputStream(file)) {
        out.write(code);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
      log.info("The following converter class has been dump at: " + file.getAbsolutePath());
    }, debugWorkers);
  }

  /**
   * Verify a class can be used as source
   *
   * @param c class
   */
  private void verifySourceType(Class<?> c) {
    Optional.ofNullable(Reflections.getDescriptionIfNotImportable(c)).ifPresent(desc -> {
      throw new VerifyException("Could not generate a converter that converts " + c.getName() + " because it's " + desc);
    });
  }

  /**
   * Verify a class can be used as target
   *
   * @param c class
   */
  private void verifyTargetType(Class<?> c) {
    Optional.ofNullable(Reflections.getDescriptionIfNotImportable(c)).ifPresent(desc -> {
      throw new VerifyException("Could not generate a converter that converts to " + c.getName() + " because it's " + desc);
    });

    Optional.ofNullable(Reflections.getDescriptionIfNotInstantiatable(c)).ifPresent(desc -> {
      throw new VerifyException("Could not generate a converter that converts to " + c.getName() + " because it's " + desc);
    });
  }

  /**
   * Return the instantiate mode for a class
   *
   * @param c class
   * @return instantiate mode
   */
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

  /**
   * Verify the given {@link java.lang.invoke.MethodHandles.Lookup} can access the given class
   *
   * @param c      class
   * @param lookup lookup
   */
  public void verifyPrivilege(Class<?> c, MethodHandles.Lookup lookup) {
    try {
      lookup.findClass(c.getName());
    } catch (IllegalAccessException e) {
      String message = (lookup == Converter.LOOKUP
          ? "This BeanCopierImpl instance can not access class: " + c.getName()
          : "The lookup defined in configuration can not access class: " + c.getName())
          + ", use `new BeanCopierImpl(f -> f.lookup(A_LOOKUP_THAT_HAS_PRIVILEGE_ACCESS))` instead";
      throw new VerifyException(message, e);
    } catch (ClassNotFoundException e) {
      var message = "%s(loaded by %s) is not visible to this BeanCopier instance with classloader: %s, use `new BeanCopierImpl(f -> f.lookup(LOOKUP_CAN_VISIT_THE_CLASS)` instead".formatted(
          c,
          c.getClassLoader(),
          lookup.lookupClass().getClassLoader()
      );
      throw new VerifyException(message, e);
    }
  }

}