package io.github.tanyaofei.beancopier.core.annotation;

import io.github.tanyaofei.beancopier.ConverterConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author tanyaofei
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Definition {

  /**
   * @see ConverterConfiguration#isSkipNull()
   */
  boolean skipNull();

  /**
   * @see ConverterConfiguration#isPreferNested() ()
   */
  boolean preferNested();

  /**
   * @see ConverterConfiguration#isPropertySupported()
   */
  boolean propertySupported();

  /**
   * @see ConverterConfiguration#isFullTypeMatching()
   */
  boolean fullTypeMatching();


}
