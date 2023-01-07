package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.exception.CopyException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BeanCopierImpl {

  /**
   * 转换器缓存默认容量
   */
  private static final int DEFAULT_CACHES_CAPACITY = 64;

  /**
   * 转换器缓存
   */
  private final
  Map<String, ? super Converter<?, ?>> caches;

  /**
   * 转换器工厂
   */
  private final ConverterFactory converterFactory = new ConverterFactory(
      new ConverterClassLoader(BeanCopierImpl.class.getClassLoader()),
      NamingPolicy.getDefault(),
      BeanCopierConfiguration.CONVERTER_CLASS_DUMP_PATH
  );

  public BeanCopierImpl(int cachesCapacity) {
    this.caches = new ConcurrentHashMap<>(cachesCapacity);
  }

  public BeanCopierImpl() {
    this(DEFAULT_CACHES_CAPACITY);
  }


  /**
   * 拷贝对象
   *
   * @param source      拷贝来源, 如果该参数为 null 则返回 null
   * @param targetClass 拷贝目标类
   * @param <S>         拷贝来源类
   * @param <T>         拷贝目标类
   * @return 拷贝结果
   */
  @Contract("null, _ -> null")
  public <S, T> T copy(@Nullable S source, @NotNull Class<T> targetClass) {
    return copy(source, targetClass, null);
  }

  /**
   * 批量克隆对象
   *
   * @param sources 被克隆对象集合, 该集合不能为 null, 但集合的元素可以为 null
   * @param <T>     克隆元素类
   * @return 克隆结果集合
   * @see #cloneList(Collection, Callback)
   */
  @NotNull
  public <T> List<T> cloneList(@NotNull Collection<T> sources) {
    return cloneList(sources, null);
  }

  /**
   * 批量克隆对象
   *
   * @param objs  被克隆对象集合, 该集合不能为 null, 但集合的元素可以为 null
   * @param callback 如果这个参数不为 null 时, 每一个克隆对象被克隆时都会调用此接口
   * @param <T>      克隆对象类
   * @return {@link ArrayList} 克隆结果列表
   */
  @SuppressWarnings("unchecked")
  public <T> List<T> cloneList(@NotNull Collection<T> objs, @Nullable Callback<T, T> callback) {
    if (objs.isEmpty()) {
      return new ArrayList<>();
    }

    int i = 0;
    List<T> ret = new ArrayList<>(objs.size());
    Iterator<T> itr = objs.iterator();

    // 因为 source 有可能出现 null, 因此要一致迭代到不为 null 才能正确获得 class
    while (itr.hasNext()) {
      T next = itr.next();
      if (next == null) {
        ret.add(null);
      } else {
        Class<T> clazz = (Class<T>) next.getClass();
        ret.add(copy(next, clazz, callback));
        ret.addAll(copyList(itr, clazz, callback, objs.size() - i));
        return ret;
      }
    }

    return ret;
  }

  /**
   * 批量对象拷贝
   *
   * @param sources     拷贝来源集合
   * @param targetClass 拷贝目标类
   * @param <S>         拷贝来源类
   * @param <T>         拷贝目标类
   * @return {@link ArrayList} 拷贝结果列表
   */
  public <S, T> List<T> copyList(Collection<S> sources, Class<T> targetClass) {
    return copyList(sources, targetClass, null);
  }

  /**
   * 批量对象拷贝
   *
   * @param source      拷贝来源
   * @param targetClass 拷贝目标类
   * @param callback    列表里每个对象拷贝完之后进行的回调操作
   * @param <S>         拷贝来源类
   * @param <T>         拷贝目标类
   * @return {@link ArrayList} 拷贝结果列表
   */
  @NotNull
  public <S, T> List<T> copyList(
      @NotNull Collection<S> source,
      @NotNull Class<T> targetClass,
      @Nullable Callback<S, T> callback
  ) {
    return copyList(source.iterator(), targetClass, callback, source.size());
  }

  /**
   * 批量对象拷贝
   *
   * @param itr         拷贝来源
   * @param targetClass 拷贝目标类
   * @param callback    列表里每个对象拷贝完之后进行的回调操作
   * @param <S>         拷贝来源类
   * @param <T>         拷贝目标类
   * @return {@link ArrayList} 拷贝结果列表
   */
  @NotNull
  @SuppressWarnings("unchecked")
  private <S, T> List<T> copyList(
      @NotNull Iterator<S> itr,
      @NotNull Class<T> targetClass,
      @Nullable Callback<S, T> callback,
      int size
  ) {
    if (!itr.hasNext()) {
      return new ArrayList<>();
    }

    // 因为 itr 可能一直返回 null, 因此要一直迭代到第一个不为 null 才能正确获取 class
    Converter<S, T> converter = null;
    List<T> ret = size > 0 ? new ArrayList<>(size) : new ArrayList<>();
    while (itr.hasNext()) {
      S source = itr.next();
      T target;
      if (source == null) {
        target = null;
      } else if (converter == null) {
        Class<S> sourceClass = (Class<S>) source.getClass();
        String cacheKey = cacheKey(sourceClass, targetClass);
        converter = generateConverter(cacheKey, sourceClass, targetClass);
        target = converter.convert(source);
      } else {
        target = converter.convert(source);
      }

      ret.add(target);
      if (callback != null) {
        callback.apply(source, target);
      }
    }

    return ret;
  }

  /**
   * 对象拷贝
   * <p>如果需要批量拷贝对象, 使用 {@link #copyList(Collection, Class)} 可以提供更好的性能</p>
   *
   * @param source      拷贝来源
   * @param targetClass 拷贝目标类
   * @param <S>         拷贝来源
   * @param <T>         拷贝目标
   * @param callback    拷贝完之后进行的操作
   * @return 拷贝结果
   * @see ConverterFactory#generateConverter(Class, Class) 动态生成 Source to Target
   * 的转换器
   */
  @Contract("null, _, _ -> null")
  @SuppressWarnings("unchecked")
  public <S, T> T copy(
      @Nullable S source,
      @NotNull(exception = NullPointerException.class) Class<T> targetClass,
      @Nullable Callback<S, T> callback
  ) {
    if (source == null) {
      return null;
    }

    Class<S> sourceClass = (Class<S>) source.getClass();
    Converter<S, T> converter = generateConverter(
        cacheKey(sourceClass, targetClass),
        sourceClass,
        targetClass
    );

    // init a target, and copy fields from source
    T target;
    try {
      target = converter.convert(source);
    } catch (Exception e) {
      throw new CopyException("Failed to copy object", e);
    }

    if (callback != null) {
      callback.apply(source, target);
    }
    return target;
  }

  /**
   * 克隆对象
   * <p>如果需要批量去克隆对象的, 使用 {@link #cloneList(Collection)} 可以提供更好的性能</p>
   *
   * @param source 被克隆对象, 如果该参数为 null 则返回 null
   * @param <T>    克隆对象类
   * @return 克隆结果
   */
  @Contract("null -> null")
  @SuppressWarnings("unchecked")
  public <T> T clone(@Nullable T source) {
    if (source == null) {
      return null;
    }
    return copy(source, (Class<T>) source.getClass(), null);
  }


  /**
   * @param cacheKey 缓存 Key
   * @param source   拷贝来源类
   * @param target   拷贝目标类
   * @param <S>      拷贝来源类
   * @param <T>      拷贝目标类
   * @return 来源拷贝到目标的转换器
   * @// TODO: 2023/1/7 在并发情况下可能同时生成两个 converter, 但是问题不大
   */
  @SuppressWarnings("unchecked")
  private <S, T> Converter<S, T> generateConverter(
      @NotNull String cacheKey,
      @NotNull Class<S> source,
      @NotNull Class<T> target
  ) {
    return (Converter<S, T>) caches.computeIfAbsent(
        cacheKey,
        key -> converterFactory.generateConverter(source, target)
    );
  }

  /**
   * 生成缓存 key
   * <p>格式 sourceClass:targetClass</p>
   *
   * @param sourceClass 拷贝来源类
   * @param targetClass 拷贝目标类
   * @return 缓存 key
   */
  private String cacheKey(
      Class<?> sourceClass, Class<?> targetClass
  ) {
    return sourceClass.getName() + ":" + targetClass.getName();
  }

}
