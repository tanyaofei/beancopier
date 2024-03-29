package io.github.tanyaofei.beancopier.core.local;

import io.github.tanyaofei.beancopier.core.local.LocalDefiner.RootLocalDefiner;
import io.github.tanyaofei.beancopier.core.local.impl.*;

/**
 * A tool for obtaining an ordered list of {@link LocalDefiner} instances.
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
  }

  /**
   * Return an ordered list of local definer instances.
   *
   * @return LocalDefiner instance
   */
  public static LocalDefiner getDefiner() {
    return theDefiner;
  }


}
