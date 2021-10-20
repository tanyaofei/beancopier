package com.github.tanyaofei.beancopier;

/**
 * @author 谭耀飞
 * @since 2021.05.0
 */
@FunctionalInterface
public interface Callback<S, T> {

  /**
   * 执行
   *
   * @param source 拷贝来源
   * @param target 拷贝结果
   */
  void apply(S source, T target);

}
