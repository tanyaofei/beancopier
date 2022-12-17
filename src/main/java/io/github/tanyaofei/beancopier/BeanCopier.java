package io.github.tanyaofei.beancopier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 对象拷贝器
 *
 * @author tanyaofei
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanCopier {

  /**
   * 转换器缓存
   */
  private static final
  ConcurrentMap<String, ? super Converter<?, ?>> CONVERTER_CACHES = new ConcurrentHashMap<>(64);

  /**
   * 类加载器
   */
  private static final ConverterClassLoader CLASS_LOADER = new ConverterClassLoader(
      BeanCopier.class.getClassLoader()
  );

  /**
   * 转换器生成工具
   */
  private static final ConverterFactory CONVERTER_FACTORY = new ConverterFactory();

  /**
   * @param source      拷贝来源
   * @param targetClass 拷贝目标类
   * @param <S>         拷贝来源
   * @param <T>         拷贝目标
   * @return 拷贝结果
   */

  public static <S, T> T copy(S source, Class<T> targetClass) {
    return copy(source, targetClass, null);
  }

  @SuppressWarnings("unchecked")
  public static <T> T clone(T source) {
    return copy(source, (Class<T>) source.getClass(), null);
  }

  public static <T> List<T> cloneList(List<T> sources) {
    return cloneList(sources, null);
  }

  @SuppressWarnings("unchecked")
  public static <T> List<T> cloneList(List<T> sources, Callback<T, T> callback) {
    if (sources.isEmpty()) {
      return new ArrayList<>();
    }
    return copyList(sources, (Class<T>) sources.get(0).getClass(), callback);
  }


  /**
   * 批量对象拷贝
   *
   * @param sources     拷贝来源列表
   * @param targetClass 拷贝目标类
   * @param <S>         拷贝来源
   * @param <T>         拷贝目标
   * @return 拷贝结果
   */

  public static <S, T> List<T> copyList(List<S> sources, Class<T> targetClass) {
    return sources.stream().map(s -> copy(s, targetClass)).collect(Collectors.toList());
  }

  /**
   * 批量对象拷贝
   *
   * @param source      拷贝来源
   * @param targetClass 拷贝目标类
   * @param callback    列表里每个对象拷贝完之后进行的操作
   * @param <S>         拷贝来源
   * @param <T>         拷贝目标
   * @return 拷贝结果
   */

  public static <S, T> List<T> copyList(
      List<S> source, Class<T> targetClass, Callback<S, T> callback
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
   * @see ConverterFactory#generateConverter(Class, Class, ConverterClassLoader) 动态生成 Source  to  Target
   * 的转换器
   */

  @SuppressWarnings("unchecked")
  private static <S, T> T copy(
      S source,
      Class<T> targetClass,
      Callback<S, T> callback
  ) {
    Converter<S, T> converter = (Converter<S, T>) CONVERTER_CACHES
        .computeIfAbsent(
            cacheKey(source.getClass(), targetClass),
            key -> CONVERTER_FACTORY.generateConverter((Class<S>) source.getClass(), targetClass, CLASS_LOADER)
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

  private static Converter<?, ?> cacheAndReturn(
      String key, Converter<?, ?> converter
  ) {
    CONVERTER_CACHES.put(key, converter);
    return converter;
  }

}
