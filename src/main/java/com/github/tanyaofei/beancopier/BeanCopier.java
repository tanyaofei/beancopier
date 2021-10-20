package com.github.tanyaofei.beancopier;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import com.github.tanyaofei.beancopier.exception.CopyException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 对象拷贝器
 * <p>经过测试, 一亿个对象拷贝速度(不递归拷贝) 耗时 15 秒, 如果用 ModelMapper 耗时为 149 秒</p>
 * <p>递归拷贝将会大大降低性能, 如果对象没有可递归拷贝的字段, 切勿使用递归拷贝 </p>
 *
 * @author 谭耀飞
 * @see BeanCopier#copy(Object, Class) 非递归拷贝
 * @see BeanCopier#copyList(List, Class) 非递归拷贝
 * @since 2021.04.0
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
      "converter", BeanCopier.class.getClassLoader()
  );

  /**
   * 转换器生成工具
   */
  private static final ConverterMaker CONVERTER_MAKER = new ConverterMaker();

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
   * @see ConverterMaker#generateConverter(Class, Class, ConverterClassLoader) 动态生成 Source -> Target
   * 的转换器
   */

  @SuppressWarnings("unchecked")
  private static <S, T> T copy(
      S source,
      Class<T> targetClass,
      Callback<S, T> callback
  ) {
    var key = cacheKey(source.getClass(), targetClass);

    var converter = (Converter<S, T>) Optional
        .ofNullable(CONVERTER_CACHES.get(key))
        .orElseGet(() -> cacheAndReturn(
            key, CONVERTER_MAKER.generateConverter(
                (Class<S>) source.getClass(), targetClass, CLASS_LOADER))
        );

    // init a target, and copy fields from source
    var target = converter.convert(source);

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

  private static CopyException exception(String msg, Throwable cause) {
    return new CopyException(msg, cause);
  }


}
