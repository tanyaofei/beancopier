package io.github.tanyaofei.beancopier.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author tanyaofei
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Alias {

  /**
   * @return alias
   */
  String value();

  /**
   * This alias is only works when the source class is in specified classes
   *
   * @return specified source classes
   */
  Class<?>[] forType();
}
