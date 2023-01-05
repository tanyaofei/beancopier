package io.github.tanyaofei.beancopier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 对象拷贝器
 * <p>该拷贝器维护着一个使用 ASM 生成的转换器缓存{@link #CONVERTER_CACHES}, 该缓存在程序运行过程中不会释放</p>
 * <p>也可以通过 {@link io.github.tanyaofei.beancopier.annotation.Property} 去定义拷贝时的一些特性</p>
 *
 * @author tanyaofei
 * @see Converter
 * @see io.github.tanyaofei.beancopier.annotation.Property
 * @see io.github.tanyaofei.beancopier.typehandler.TypeHandler
 * @see io.github.tanyaofei.beancopier.annotation.Feature
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanCopier {

  /**
   * 转换器缓存默认容量
   */
  private static final int DEFAULT_CACHE_CAPACITY = 64;

  /**
   * 转换器缓存
   */
  private static final
  ConcurrentMap<String, ? super Converter<?, ?>> CONVERTER_CACHES = new ConcurrentHashMap<>(DEFAULT_CACHE_CAPACITY);

  /**
   * 转换器工厂
   */
  private static final ConverterFactory CONVERTER_FACTORY = new ConverterFactory(
      new ConverterClassLoader(BeanCopier.class.getClassLoader()),
      Type.getInternalName(ConverterFactory.class) + "/generate/converter",
      BeanCopierConfiguration.CONVERTER_CLASS_DUMP_PATH
  );

  /**
   * @param source      拷贝来源, 如果该参数为 null 则返回 null
   * @param targetClass 拷贝目标类
   * @param <S>         拷贝来源类
   * @param <T>         拷贝目标类
   * @return 拷贝结果
   */
  @Contract("null, _ -> null")
  public static <S, T> T copy(@Nullable S source, @NotNull Class<T> targetClass) {
    return copy(source, targetClass, null);
  }

  /**
   * 克隆对象
   *
   * @param source 被克隆对象, 如果该参数为 null 则返回 null
   * @param <T>    克隆对象类
   * @return 克隆结果
   */
  @Contract("null -> null")
  @SuppressWarnings("unchecked")
  public static <T> T clone(@Nullable T source) {
    if (source == null) {
      return null;
    }
    return copy(source, (Class<T>) source.getClass(), null);
  }

  /**
   * 批量克隆对象
   *
   * @param sources 被克隆对象集合, 该集合不能为 null, 但集合的元素可以为 null
   * @param <T> 克隆元素类
   * @return 克隆结果集合
   * @see #cloneList(Collection, Callback)
   */
  @NotNull
  public static <T> List<T> cloneList(@NotNull Collection<T> sources) {
    return cloneList(sources, null);
  }

  /**
   * 批量克隆对象
   *
   * @param sources  被克隆对象集合, 该集合不能为 null, 但集合的元素可以为 null
   * @param callback 如果这个参数不为 null 时, 每一个克隆对象被克隆时都会调用此接口
   * @param <T>      克隆对象类
   * @return {@link ArrayList} 克隆结果列表
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> cloneList(@NotNull Collection<T> sources, @Nullable Callback<T, T> callback) {
    if (sources.isEmpty()) {
      return new ArrayList<>();
    }
    return copyList(sources, (Class<T>) sources.iterator().next().getClass(), callback);
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
  public static <S, T> List<T> copyList(Collection<S> sources, Class<T> targetClass) {
    return sources.stream().map(s -> copy(s, targetClass)).collect(Collectors.toList());
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
  public static <S, T> List<T> copyList(
      @NotNull Collection<S> source,
      @NotNull Class<T> targetClass,
      @Nullable Callback<S, T> callback
  ) {
    return source
        .stream()
        .map(s -> copy(s, targetClass, callback))
        .collect(Collectors.toList());
  }

  /**
   * 对象拷贝
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
  private static <S, T> T copy(
      @Nullable S source,
      @NotNull(exception = NullPointerException.class) Class<T> targetClass,
      @Nullable Callback<S, T> callback
  ) {
    if (source == null) {
      return null;
    }

    Converter<S, T> converter = (Converter<S, T>) CONVERTER_CACHES.computeIfAbsent(
        cacheKey(source.getClass(), targetClass),
        key -> CONVERTER_FACTORY.generateConverter((Class<S>) source.getClass(), targetClass)
    );

    // init a target, and copy fields from source
    T target = converter.convert(source);

    // 执行
    if (callback != null) {
      callback.apply(source, target);
    }
    return target;
  }

  /**
   * 生成缓存 key
   * <p>格式 sourceClass:targetClass</p>
   *
   * @param sourceClass 拷贝来源类
   * @param targetClass 拷贝目标类
   * @return 缓存 key
   */
  private static String cacheKey(
      Class<?> sourceClass, Class<?> targetClass
  ) {
    return sourceClass.getName() + ":" + targetClass.getName();
  }


}
