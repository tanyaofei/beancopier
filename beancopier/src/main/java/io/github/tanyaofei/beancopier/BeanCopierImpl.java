package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.converter.Converter;
import io.github.tanyaofei.beancopier.core.ConverterFactory;
import io.github.tanyaofei.beancopier.exception.CopyException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
   * Create a BeanCopierImpl with the default configurations.
   */
  @Contract(pure = true)
  public BeanCopierImpl() {
    this(DEFAULT_CACHE_CAPACITY);
  }

  /**
   * Create an instance with a cache that has the specified initial capacity.
   *
   * @param cachesInitialCapacity The initial capacity of cache
   */
  @Contract(pure = true)
  public BeanCopierImpl(int cachesInitialCapacity) {
    this(
        cachesInitialCapacity,
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
  private BeanCopierImpl(int cacheInitialCapacity, @NotNull ConverterFactory converterFactory) {
    this.cache = new ConcurrentHashMap<>(cacheInitialCapacity);
    this.converterFactory = converterFactory;
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
  public BeanCopierImpl(Consumer<ConverterConfiguration.Builder> config) {
    this(DEFAULT_CACHE_CAPACITY, new ConverterFactory(config));
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
  public BeanCopierImpl(int cacheInitialCapacity, Consumer<ConverterConfiguration.Builder> config) {
    this(cacheInitialCapacity, new ConverterFactory(config));
  }

  /**
   * Return a default instance for {@link BeanCopier}, the {@link Converter} instances and classes will not be GC.
   *
   * @return The default instance for {@link BeanCopier}
   */
  @Contract
  static BeanCopierImpl getInstance() {
    return Lazy.INSTANCE;
  }


  @Contract(value = "null, _ -> null")
  public <S, T> T copy(@Nullable S source, @NotNull Class<T> target) {
    return copy(source, target, null);
  }

  @SuppressWarnings("unchecked")
  @Contract(value = "null, _, _ -> null")
  public <S, T> T copy(
      @Nullable S source,
      @NotNull Class<T> target,
      @Nullable BiConsumer<S, T> consumer
  ) {
    if (source == null) {
      return null;
    }

    var sourceType = (Class<S>) source.getClass();
    var converter = generateConverter(new CacheKey(sourceType, target), sourceType, target);

    // init a t, and copy fields from source
    T t;
    try {
      t = converter.convert(source);
      if (consumer != null) {
        consumer.accept(source, t);
      }
    } catch (Exception e) {
      throw new CopyException("Failed to copy bean with the following converter: " + converter.getClass().getName(), e);
    }

    return t;
  }

  @SuppressWarnings("unchecked")
  @Contract(value = "null -> null")
  public <T> T clone(@Nullable T source) {
    if (source == null) {
      return null;
    }
    return copy(source, (Class<T>) source.getClass(), null);
  }


  @NotNull
  public <T> List<T> cloneList(@NotNull Collection<@Nullable T> sources) {
    return cloneList(sources, null);
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> cloneList(
      @NotNull Collection<@Nullable T> objs,
      @Nullable BiConsumer<T, T> consumer
  ) {
    if (objs.isEmpty()) {
      return new ArrayList<>();
    }

    int remains = objs.size();
    var ret = new ArrayList<T>(remains);

    if (objs instanceof List<T> objList) {
      var itr = objList.listIterator();
      while (itr.hasNext()) {
        T t = itr.next();
        if (t == null) {
          remains--;
          ret.add(null);
          continue;
        }

        itr.previous();
        Class<T> c = (Class<T>) t.getClass();
        assert remains + ret.size() == objs.size();
        ret.addAll(copyList(itr, c, consumer, remains));
      }
    } else {
      var itr = objs.iterator();
      while (itr.hasNext()) {
        T t = itr.next();
        if (t == null) {
          remains--;
          ret.add(null);
          continue;
        }

        var c = (Class<T>) t.getClass();
        ret.add(copy(t, c, consumer));
        assert remains - 1 + ret.size() == objs.size();
        ret.addAll(copyList(itr, c, consumer, remains - 1));
        return ret;
      }
    }

    assert objs.size() == ret.size();
    return ret;
  }

  public <S, T> List<T> copyList(
      @NotNull Collection<@Nullable S> sources,
      @NotNull Class<T> target
  ) {
    return copyList(sources, target, null);
  }

  @NotNull
  public <S, T> List<T> copyList(
      @NotNull Collection<@Nullable S> source,
      @NotNull Class<T> target,
      @Nullable BiConsumer<S, T> consumer
  ) {
    return copyList(source.iterator(), target, consumer, source.size());
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
  @SuppressWarnings("unchecked")
  private <S, T> Converter<S, T> generateConverter(
      @NotNull CacheKey cacheKey,
      @NotNull Class<S> sourceType,
      @NotNull Class<T> targetType
  ) {
    return (Converter<S, T>) cache.computeIfAbsent(
        cacheKey,
        key -> converterFactory.generateConverter(sourceType, targetType)
    );
  }

  @NotNull
  @SuppressWarnings("unchecked")
  private <S, T> List<T> copyList(
      @NotNull Iterator<@Nullable S> itr,
      @NotNull Class<T> tc,
      @Nullable BiConsumer<S, T> consumer,
      int initialCapacity
  ) {
    if (!itr.hasNext()) {
      return new ArrayList<>();
    }

    // 因为 itr 可能一直返回 null, 因此要一直迭代到第一个不为 null 才能正确获取 class
    Converter<S, T> c = null;
    var ret = initialCapacity > 0 ? new ArrayList<T>(initialCapacity) : new ArrayList<T>();
    while (itr.hasNext()) {
      var s = itr.next();
      T t;
      if (s == null) {
        t = null;
      } else if (c == null) {
        var sc = (Class<S>) s.getClass();
        var cacheKey = new CacheKey(sc, tc);
        c = generateConverter(cacheKey, sc, tc);
        t = c.convert(s);
      } else {
        t = c.convert(s);
      }

      ret.add(t);
      if (consumer != null) {
        consumer.accept(s, t);
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

  /**
   * The key object for {@link #cache}.
   *
   * @author tanyaofei
   * @since 0.1.4
   */
  record CacheKey(Class<?> sc, Class<?> tc) {

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof CacheKey o)) {
        return false;
      }

      return o.sc.equals(sc) && o.tc.equals(tc);
    }

  }
}
