package io.github.tanyaofei.beancopier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Collection;
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
   * 转换器工厂
   */
  private static final ConverterFactory CONVERTER_FACTORY = new ConverterFactory(
          new ConverterClassLoader(BeanCopier.class.getClassLoader()),
          Type.getInternalName(ConverterFactory.class) + "/generate/converter",
          BeanCopierConfiguration.CONVERTER_CLASS_DUMP_PATH
  );

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

  /**
   * 克隆对象
   *
   * @param source 被克隆对象
   * @return 克隆结果
   */
  @SuppressWarnings("unchecked")
  public static <T> T clone(T source) {
    return copy(source, (Class<T>) source.getClass(), null);
  }

  /**
   * 批量克隆对象
   *
   * @param sources 被克隆对象集合
   * @return 克隆结果集合
   */
  public static <T> List<T> cloneList(Collection<T> sources) {
    return cloneList(sources, null);
  }

  @SuppressWarnings("unchecked")
  public static <T> List<T> cloneList(Collection<T> sources, Callback<T, T> callback) {
    if (sources.isEmpty()) {
      return new ArrayList<>();
    }
    return copyList(sources, (Class<T>) sources.iterator().next().getClass(), callback);
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
          Collection<S> source, Class<T> targetClass, Callback<S, T> callback
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
  @SuppressWarnings("unchecked")
  private static <S, T> T copy(
      S source,
      Class<T> targetClass,
      Callback<S, T> callback
  ) {
    if (targetClass == null) {
      throw new NullPointerException("targetClass is null");
    }

    Converter<S, T> converter = (Converter<S, T>) CONVERTER_CACHES
        .computeIfAbsent(
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

  private static Converter<?, ?> cacheAndReturn(
      String key, Converter<?, ?> converter
  ) {
    CONVERTER_CACHES.put(key, converter);
    return converter;
  }

}
