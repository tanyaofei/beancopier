package io.github.tanyaofei.beancopier.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author tanyaofei
 * @see Property#alias()
 * @since 0.2.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Alias {

  /**
   * @return alias
   */
  String value();

  /**
   * This {@link Alias} is only works when the source class is in it
   *
   * @return specified source classes
   */
  Class<?>[] forType();

}
