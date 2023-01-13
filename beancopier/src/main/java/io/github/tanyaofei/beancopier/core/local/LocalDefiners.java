package io.github.tanyaofei.beancopier.core.local;

import io.github.tanyaofei.beancopier.core.local.LocalDefiner.RootLocalDefiner;
import io.github.tanyaofei.beancopier.core.local.impl.*;

/**
 * 组织好顺序的局部变量定义器实例获取工具
 */
public abstract class LocalDefiners {

  private static final LocalDefiner theDefiner = new RootLocalDefiner();

  static {
    theDefiner
        .fallbackTo(new SkippedLocalDefiner())
        .fallbackTo(new NoNameMatchLocalDefiner())
        .fallbackTo(new CompatibleLocalDefiner())
        .fallbackTo(new NestedLocalDefiner())
        .fallbackTo(new CollectionNestedLocalDefiner())
        .fallbackTo(new ZeroValueLocalDefiner())
        .fallbackTo(new NoMoreFallbackLocalDefiner());
  }

  /**
   * 获取已经组织好定义顺序的局部变量定义器
   *
   * @return 局部变量定义器
   */
  public static LocalDefiner getDefiner() {
    return theDefiner;
  }


}
