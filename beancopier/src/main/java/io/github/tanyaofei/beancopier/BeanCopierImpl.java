package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.converter.Converter;
import io.github.tanyaofei.beancopier.core.ConverterFactory;
import io.github.tanyaofei.beancopier.exception.CopyException;
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
   * Return a default instance for {@link BeanCopier}, the {@link Converter} instances and classes will not be GC.
   *
   * @return The default instance for {@link BeanCopier}
   */
  @Contract
  static BeanCopierImpl getInstance() {
    return Lazy.INSTANCE;
  }


  /**
   * Create a BeanCopierImpl with the default configurations.
   */
  @Contract(pure = true)
  public BeanCopierImpl() {
    this(DEFAULT_CACHE_CAPACITY);
  }

  /**
   * Create an instance with a cache that has the specified initial capacity.
   *
   * @param cacheInitialCapacity The initial capacity of cache
   */
  @Contract(pure = true)
  public BeanCopierImpl(int cacheInitialCapacity) {
    this(
        cacheInitialCapacity,
        config -> {
        }
    );
  }

  /**
   * create an instance
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
   * Create an instance with custom configurations and a cache that has the specified initial capacity
   * <pre>{@code
   *  BeanCopierImpl beanCopier = new BeanCopierImpl(
   *    config -> config.classLoader(this.getClassLoader())
   *  );
   * }
   * </pre>
   *
   * @param cacheInitialCapacity the initial capacity of cache
   * @param config               the config will be used
   */
  @Contract(pure = true)
  public BeanCopierImpl(
      @Nonnegative int cacheInitialCapacity,
      @NotNull Consumer<ConverterConfiguration.Builder> config
  ) {
    this(cacheInitialCapacity, new ConverterFactory(config));
  }

  /**
   * Create an instance with custom configurations
   * <pre>{@code
   *  BeanCopierImpl beanCopier = new BeanCopierImpl(
   *    config -> config.classLoader(this.getClassLoader())
   *  );
   * }
   * </pre>
   *
   * @param config config will be used
   */
  @Contract(pure = true)
  public BeanCopierImpl(@NotNull Consumer<ConverterConfiguration.Builder> config) {
    this(DEFAULT_CACHE_CAPACITY, new ConverterFactory(config));
  }

  /**
   * clone a object
   *
   * @param source the object is about to clone
   * @param <T>    the type of source
   * @return cloned object, null if the source is null
   */
  @Contract(pure = true)
  public <T> T clone(@Nullable T source) {
    return clone(source, (BiConsumer<T, T>) null);
  }

  @SuppressWarnings("unchecked")
  @Contract(value = "null, _ -> null", pure = true)
  public <T> T clone(@Nullable T source, @Nullable BiConsumer<T, T> afterCloned) {
    if (source == null) {
      if (afterCloned != null) {
        afterCloned.accept(null, null);
      }
      return null;
    }
    return copy(source, (Class<T>) source.getClass(), afterCloned);
  }

  /**
   * copy an object to a specified class
   *
   * @param source the object that is about to be copied
   * @param target specified class that the source will copy to it
   * @param <S>    the type of source
   * @param <T>    the type of target
   * @return copy object, null if the source is null
   */
  @Contract(value = "null, _ -> null", pure = true)
  public <S, T> T copy(@Nullable S source, @NotNull Class<T> target) {
    return copy(source, target, (BiConsumer<S, T>) null);
  }

  /**
   * Copy an object to a specified class,
   * specify an {@link BiConsumer} {@code afterCopied} to custom what you want to do before returning after copy.
   *
   * @param source    the object that is about to be copied
   * @param target    specified class that the source will copy to it
   * @param afterCopied custom operations before returning after copy
   * @param <S>       the type of source
   * @param <T>       the type of target
   * @return copy object, null if the source is null
   * @throws CopyException if any exception occur during the copying process and the {@code afterCopied}
   */
  @SuppressWarnings("unchecked")
  @Contract(value = "null, _, _ -> null", pure = true)
  public <S, T> T copy(
      @Nullable S source,
      @NotNull Class<T> target,
      @Nullable BiConsumer<S, T> afterCopied
  ) {
    if (source == null) {
      if (afterCopied != null) {
        afterCopied.accept(null, null);
      }
      return null;
    }

    var sourceType = (Class<S>) source.getClass();
    var converter = generateConverter(new CacheKey(sourceType, target), sourceType, target);

    // init a t, and copy fields from source
    T t;
    try {
      t = converter.convert(source);
      if (afterCopied != null) {
        afterCopied.accept(source, t);
      }
    } catch (Exception e) {
      throw new CopyException("Failed to copy bean with the following converter: " + converter.getClass().getName(), e);
    }

    return t;
  }

  /**
   * Clone a collection of sources.
   * Each element in the collection will be copied into a new collection is about to returning,
   * if the element is null, it will be copied as null into the new collection
   *
   * @param sources sources that is about to copy
   * @param <T>     the type of source
   * @return new collection contains copy objects
   * @throws NullPointerException if the {@code sources} is null
   */
  @Nonnull
  @Contract(pure = true)
  public <T> List<T> cloneList(@NotNull Collection<@Nullable T> sources) {
    return cloneList(sources, null);
  }

  /**
   * Clone a collection of sources.
   * Each element in the collection will be cloned into a new collection is about to returning,
   * if the element is null, it will be cloned as null into the new collection.
   * Specify an {@link BiConsumer} {@code afterEachCloned} to custom what you want to do after each element is cloned.
   *
   * @param sources   collection that is about to be cloned
   * @param afterEachCloned custom operation after each element is cloned
   * @param <T>       the type of source
   * @return new collection contains cloned objects
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
        if (afterEachCloned != null) {
          afterEachCloned.accept(null, null);
        }
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
   * Copy a collection of sources.
   * Each element in the collection will be copied into a new collection is about to returning,
   * if the element is null, it will be copied as null into the new collection.
   *
   * @param sources collection that is about to be copied
   * @param target  the type is about to be copied to
   * @param <S>     the type of source
   * @param <T>     the type of target
   * @return new collection contains copy object
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
   * Copy a collection of sources.
   * Each element in the collection will be copied into a new collection is about to returning,
   * if the element is null, it will be copied as null into the new collection.
   * Specify an {@link BiConsumer} {@code afterCopy} to custom what you want to do after each element is copied.
   *
   * @param sources collection that is about to be copied
   * @param target  the type is about to be copied to
   * @param <S>     the type of source
   * @param <T>     the type of target
   * @return new collection contains copy object
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
   * Return a converter that copies the source object to the target object. The converter should be created if there is no instance in cache.
   *
   * @param cacheKey   the key for cache.
   * @param sourceType the class of source
   * @param targetType the class of target
   * @param <S>        the type of source
   * @param <T>        the type of target
   * @return a converter that copies the source object to the target object
   */
  @Nonnull
  @SuppressWarnings("unchecked")
  private <S, T> Converter<S, T> generateConverter(
      @Nonnull CacheKey cacheKey,
      @Nonnull Class<S> sourceType,
      Class<T> targetType
  ) {
    return (Converter<S, T>) cache.computeIfAbsent(
        cacheKey,
        key -> converterFactory.generateConverter(sourceType, targetType)
    );
  }

  @Contract
  @SuppressWarnings("unchecked")
  private <S, T> List<T> copyList(
      @Nonnull Iterator<S> sources,
      @Nonnull Class<T> targetType,
      @Nullable BiConsumer<S, T> afterEachCopied,
      int initialCapacity
  ) {
    if (!sources.hasNext()) {
      return new ArrayList<>();
    }

    // 因为 sources 可能一直返回 null, 因此要一直迭代到第一个不为 null 才能正确获取 class
    Converter<S, T> converter = null;
    var ret = initialCapacity > 0
              ? new ArrayList<T>(initialCapacity)
              : new ArrayList<T>();
    while (sources.hasNext()) {
      var source = sources.next();
      T target;
      if (source == null) {
        target = null;
      } else if (converter == null) {
        var sourceType = (Class<S>) source.getClass();
        converter = generateConverter(new CacheKey(sourceType, targetType), sourceType, targetType);
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
        new ConverterFactory(builder -> builder.classDumpPath(BeanCopierConfiguration.CONVERTER_CLASS_DUMP_PATH))
    );
  }

  private record CacheKey(
      String sourceTypeClassLoader,
      String sourceType,
      String targetTypeClassLoader,
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
