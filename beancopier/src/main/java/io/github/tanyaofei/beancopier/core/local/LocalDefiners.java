package io.github.tanyaofei.beancopier.core.local;

import io.github.tanyaofei.beancopier.core.local.LocalDefiner.RootLocalDefiner;
import io.github.tanyaofei.beancopier.core.local.impl.*;
import org.jetbrains.annotations.NotNull;


/**
 * A tool for obtaining an ordered list of {@link LocalDefiner} instances.
 */
public abstract class LocalDefiners {

  private static final LocalDefiner theDefiner = new RootLocalDefiner();

  static {
    theDefiner
        .fallbackTo(new SkippedLocalDefiner())
        .fallbackTo(new NoNameMatchedLocalDefiner())
        .fallbackTo(new CompatibleLocalDefiner())
        .fallbackTo(new NestedLocalDefiner())
        .fallbackTo(new IterableNestedLocalDefiner())
        .fallbackTo(new ZeroValueLocalDefiner());
  }

  /**
   * Return an ordered list of local definer instances.
   *
   * @return LocalDefiner instance
   */
  @NotNull
  public static LocalDefiner getDefiner() {
    return theDefiner;
  }


}
