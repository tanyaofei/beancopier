package io.github.tanyaofei.beancopier;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * @author tanyaofei
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
