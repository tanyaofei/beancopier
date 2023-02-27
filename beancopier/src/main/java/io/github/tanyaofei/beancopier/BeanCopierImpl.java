package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.converter.Converter;
import io.github.tanyaofei.beancopier.core.ConverterFactory;
import io.github.tanyaofei.beancopier.exception.CopyException;
import io.github.tanyaofei.beancopier.utils.RefArrayList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * <b>Bean Copying Utils</b>
 *
 * @author tanyaofei
 * @since 0.1.0
 */
public final class BeanCopierImpl {

  /**
   * Default capacity of the converter cache.
   */
  private static final int DEFAULT_CACHE_CAPACITY = 64;

  /**
   * The converter generated for the source and target classes of the copy will be cached.
   * When the same source class is copied to the same target class in the future,
   * the converter bytecode does not need to be regenerated, but can be retrieved from this cache.
   */
  private final ConcurrentMap<CacheKey, ? super Converter<?, ?>> cache;

  /**
   * Converter Factory
   */
  private final ConverterFactory converterFactory;


  /**
   * Create an instance with default configurations and features
   */
  @Contract(pure = true)
  public BeanCopierImpl() {
    this(DEFAULT_CACHE_CAPACITY);
  }


  /**
   * Create an instance with a cache that has the given initial capacity.
   *
   * @param cacheInitialCapacity The initial capacity of cache
   */
  @Contract(pure = true)
  public BeanCopierImpl(int cacheInitialCapacity) {
    this(
        cacheInitialCapacity,
        ConverterFactory.DEFAULT_FEATURE
    );
  }



  /**
   * Create an instance with configured with given features
   * <pre>{@code
   *  BeanCopierImpl beanCopier = new BeanCopierImpl(
   *    features -> features.lookup(MethodHandlers.lookup)
   *  );
   * }
   * </pre>
   *
   * @param cacheInitialCapacity initial capacity of cache
   * @param features              features will be set
   * @throws NullPointerException if {@code features} is {@code null}
   */
  @Contract(pure = true)
  public BeanCopierImpl(
      @Nonnegative int cacheInitialCapacity,
      @NotNull Consumer<ConverterFeatures.Builder> features
  ) {
    this(cacheInitialCapacity, new ConverterFactory(features));
  }

  /**
   * Create an instance with configured with given features
   * <pre>{@code
   *  BeanCopierImpl beanCopier = new BeanCopierImpl(
   *    feature -> feature.lookup(MethodHandlers.lookup)
   *  );
   * }
   * </pre>
   *
   * @param feature feature will be set
   * @throws NullPointerException if {@code feature} is {@code null}
   */
  @Contract(pure = true)
  public BeanCopierImpl(@NotNull Consumer<ConverterFeatures.Builder> feature) {
    this(DEFAULT_CACHE_CAPACITY, new ConverterFactory(feature));
  }

  /**
   * Create an instance
   *
   * @param cacheInitialCapacity the initial capacity of cache
   * @param converterFactory     converter factory instance
   */
  @Contract(pure = true)
  private BeanCopierImpl(@Nonnegative int cacheInitialCapacity, @NotNull ConverterFactory converterFactory) {
    this.cache = new ConcurrentHashMap<>(cacheInitialCapacity);
    this.converterFactory = converterFactory;
  }


  /**
   * Let {@code consumer} consume two {@code null} values if the {@code consumer} is not {@code null}
   *
   * @param consumer nullable bi-consumer
   */
  private static void consumeNulls(@Nullable BiConsumer<?, ?> consumer) {
    if (consumer != null) {
      consumer.accept(null, null);
    }
  }

  /**
   * Return a default instance for {@link BeanCopier}
   *
   * @return default instance
   */
  @Contract
  static BeanCopierImpl getInstance() {
    return Lazy.INSTANCE;
  }

  /**
   * Clone an object
   *
   * @param source object to be cloned
   * @return cloned object or {@code null} if {@code source} is {@code null}
   */
  @Contract(value = "null -> null", pure = true)
  public <T> T clone(@Nullable T source) {
    return clone(source, null);
  }


  /**
   * Clone an object
   *
   * @param source      object to be cloned
   * @param afterCloned a function to be executed after cloned
   * @return object or {@code null} if {@code source} is {@code null}
   */
  @SuppressWarnings("unchecked")
  @Contract(value = "null, _ -> null", pure = true)
  public <T> T clone(@Nullable T source, @Nullable BiConsumer<T, T> afterCloned) {
    if (source == null) {
      consumeNulls(afterCloned);
      return null;
    }
    return copy(source, (Class<T>) source.getClass(), afterCloned);
  }

  /**
   * Clone an array
   *
   * @param sources array to be cloned
   * @return clone result
   */
  @Nonnull
  @Contract(pure = true)
  public <T> List<T> cloneList(@NotNull T[] sources) {
    return cloneList(sources, null);
  }

  /**
   * Clone an array.
   * If any element was {@code} null, it will be {@code null} at the same index in the return value.
   *
   * @param sources         array to be cloned
   * @param afterEachCloned executed after each element was cloned
   * @return clone result
   */
  @Nonnull
  @Contract(pure = true)
  @SuppressWarnings("unchecked")
  public <T> List<T> cloneList(@NotNull T[] sources, @Nullable BiConsumer<T, T> afterEachCloned) {
    return copyList(RefArrayList.of(sources), (Class<T>) sources.getClass().getComponentType(), afterEachCloned);
  }

  /**
   * Clone a collection.
   * If the element is {@code null}, it will be {@code null} at the same index in the return value.
   *
   * @param sources collection to be cloned
   * @return clone result
   * @throws NullPointerException if the {@code sources} is null
   */
  @Nonnull
  @Contract(pure = true)
  public <T> List<T> cloneList(@NotNull Collection<@Nullable T> sources) {
    return cloneList(sources, null);
  }


  /**
   * Clone a collection.
   * If the element is {@code null}, it will be {@code null} at the same index in the return value.
   * {@code afterEachCloned} will be execute on each element was cloned
   *
   * @param sources         collection that is about to be cloned
   * @param afterEachCloned custom operation after each element is cloned
   * @return new collection contains cloned objects
   * @throws NullPointerException if {@code sources} is {@code null}
   */
  @Nonnull
  @Contract(pure = true)
  @SuppressWarnings("unchecked")
  public <T> List<T> cloneList(
      @NotNull Collection<@Nullable T> sources,
      @Nullable BiConsumer<T, T> afterEachCloned
  ) {
    if (sources.isEmpty()) {
      return new ArrayList<>();
    }

    int remains = sources.size();
    var ret = new ArrayList<T>(remains);
    var itr = sources.iterator();
    T source = null;
    while (itr.hasNext() && source == null) {
      // feeding null until found a non-null source
      source = itr.next();
      if (source == null) {
        remains--;
        ret.add(null);
        consumeNulls(afterEachCloned);
      }
    }

    if (source == null) {
      return ret;
      // end loop cause no more sources
    }

    // end loop cause found a non-null source
    var c = (Class<T>) source.getClass();
    ret.add(copy(source, c, afterEachCloned));
    assert remains - 1 + ret.size() == sources.size();
    ret.addAll(copyList(itr, c, afterEachCloned, remains - 1));
    assert sources.size() == ret.size();
    return ret;
  }

  /**
   * Copy an object
   *
   * @param source object to copy from
   * @param target target class to copy to
   * @return copy result or {@code null} if {@code source} is {@code null}
   * @throws NullPointerException if {@code target} is {@code null}
   */
  @Contract(value = "null, _ -> null", pure = true)
  public <S, T> T copy(@Nullable S source, @NotNull Class<T> target) {
    return copy(source, target, null);
  }

  /**
   * Copy an object.
   *
   * @param source      object to copy from
   * @param target      target class to copy to
   * @param afterCopied custom operations before returning after copy
   * @return copy result or {@code null} if {@code source} is {@code null}
   * @throws CopyException        if any exception occur during the copying process and the {@code afterCopied}
   * @throws NullPointerException if {@code target} is {@code null}
   */
  @SuppressWarnings("unchecked")
  @Contract(value = "null, _, _ -> null", pure = true)
  public <S, T> T copy(
      @Nullable S source,
      @NotNull Class<T> target,
      @Nullable BiConsumer<S, T> afterCopied
  ) {
    if (source == null) {
      consumeNulls(afterCopied);
      return null;
    }

    var sourceType = (Class<S>) source.getClass();
    var converter = getConverter(new CacheKey(sourceType, target), sourceType, target);

    T instance;
    try {
      instance = converter.convert(source);
    } catch (Exception e) {
      throw new CopyException("Failed to copy with the following converter: " + converter.getClass().getName(), e);
    }
    if (afterCopied != null) {
      afterCopied.accept(source, instance);
    }

    return instance;
  }


  /**
   * Copy an array.
   * If the element is {@code null}, it will be {@code null} at the same index in the return value.
   *
   * @param sources array to copy from
   * @param target  type to copy to
   * @return copy result
   * @throws NullPointerException if {@code sources} or {@code target} is {@code null}
   */
  @Nonnull
  @Contract(pure = true)
  public <S, T> List<T> copyList(S[] sources, @NotNull Class<T> target) {
    return copyList(sources, target, null);
  }

  /**
   * Copy an array
   * If the element is {@code null}, it will be {@code null} at the same index in the return value.
   * {@code afterEachCopied} will be executed after each element was copied
   *
   * @param sources         array to copy from
   * @param target          type to copy to
   * @param afterEachCopied executed after each element was copied
   * @return copy result
   * @throws NullPointerException if {@code sources} or {@code target} is {@code null}
   */
  @Nonnull
  @Contract(pure = true)
  public <S, T> List<T> copyList(S[] sources, @NotNull Class<T> target, @Nullable BiConsumer<S, T> afterEachCopied) {
    return copyList(RefArrayList.of(sources), target, afterEachCopied);
  }

  /**
   * Copy a collection
   * If any element is {@code null}, it will be {@code null} at the same index in the return value.
   *
   * @param sources collection to copy from
   * @param target  type to copy to
   * @return copy result
   * @throws NullPointerException if {@code sources} or {@code target} is {@code null}
   */
  @Nonnull
  @Contract(pure = true)
  public <S, T> List<T> copyList(
      @NotNull Collection<@Nullable S> sources,
      @NotNull Class<T> target
  ) {
    return copyList(sources, target, null);
  }

  /**
   * Copy a collection
   * If any element is {@code null}, it will be {@code null} at the same index in the return value.
   * {@code afterCopyEach} will be executed after each element was copied
   *
   * @param sources       collection to copy from
   * @param target        type to copy to
   * @param afterCopyEach executed after each element was copied
   * @return copy result
   * @throws NullPointerException if {@code sources} or {@code target} is {@code null}
   */
  @Nonnull
  @Contract(pure = true)
  public <S, T> List<T> copyList(
      @NotNull Collection<@Nullable S> sources,
      @NotNull Class<T> target,
      @Nullable BiConsumer<S, T> afterCopyEach
  ) {
    return copyList(sources.iterator(), target, afterCopyEach, sources.size());
  }


  /**
   * Return a converter which can copy source to target type.
   * <br>
   * If it's not found in the {@link #cache}, this method will create an instance, return it after it was cached.
   *
   * @param cacheKey   cache key
   * @param sourceType type to copy from
   * @param targetType type to copy to
   * @return converter
   */
  @Nonnull
  @SuppressWarnings("unchecked")
  private <S, T> Converter<S, T> getConverter(
      @Nonnull CacheKey cacheKey, @Nonnull Class<S> sourceType, Class<T> targetType
  ) {
    return (Converter<S, T>) cache.computeIfAbsent(
        cacheKey,
        key -> converterFactory.genConverter(sourceType, targetType)
    );
  }

  /**
   * Copy from an iterable.
   * If any element is {@code null}, it will be {@code null} at the same index in the return value.
   *
   * @param sources         the iterable to copy from
   * @param targetType      the type to copy to
   * @param afterEachCopied will be executed after each element was copied
   * @param size            the size of {@code sources}. -1 if unknown
   * @return copy result
   */
  @Contract
  @SuppressWarnings("unchecked")
  private <S, T> List<T> copyList(
      @Nonnull Iterator<S> sources, @Nonnull Class<T> targetType, @Nullable BiConsumer<S, T> afterEachCopied, int size
  ) {
    if (!sources.hasNext()) {
      return new ArrayList<>();
    }

    Converter<S, T> converter = null;
    var ret = size > 0 ? new ArrayList<T>(size) : new ArrayList<T>();

    // find the class of source until the element is NOT null
    // so that we can get the converter
    while (sources.hasNext()) {
      var source = sources.next();
      T target;
      if (source == null) {
        target = null;
      } else if (converter == null) {
        var sourceType = (Class<S>) source.getClass();
        converter = getConverter(new CacheKey(sourceType, targetType), sourceType, targetType);
        target = converter.convert(source);
      } else {
        target = converter.convert(source);
      }

      ret.add(target);
      if (afterEachCopied != null) {
        afterEachCopied.accept(source, target);
      }
    }

    return ret;
  }

  private static class Lazy {
    private final static BeanCopierImpl INSTANCE = new BeanCopierImpl(
        DEFAULT_CACHE_CAPACITY,
        new ConverterFactory()
    );
  }

  /**
   * Cache key for {@link #cache}
   *
   * @param sourceClassLoader classloader of source
   * @param sourceType        source class
   * @param targetClassLoader classloader of target
   * @param targetType        target class
   */
  private record CacheKey(
      String sourceClassLoader,
      String sourceType,
      String targetClassLoader,
      String targetType
  ) {

    public CacheKey(@Nonnull Class<?> sourceType, @Nonnull Class<?> targetType) {
      this(
          Optional.ofNullable(sourceType.getClassLoader()).map(ClassLoader::getName).orElse(null),
          sourceType.getName(),
          Optional.ofNullable(targetType.getClassLoader()).map(ClassLoader::getName).orElse(null),
          targetType.getName()
      );
    }
  }

}
