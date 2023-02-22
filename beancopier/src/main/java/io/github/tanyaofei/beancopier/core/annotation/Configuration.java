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
@SuppressWarnings("unused")
public @interface Configuration {

  String SKIP_NULL = "skipNull";

  /**
   * @see ConverterConfiguration#isSkipNull()
   */
  boolean skipNull();

  String PREFER_NESTED = "preferNested";

  /**
   * @see ConverterConfiguration#isPreferNested() ()
   */
  boolean preferNested();

  String PROPERTY_SUPPORTED = "propertySupported";

  /**
   * @see ConverterConfiguration#isPropertySupported()
   */
  boolean propertySupported();

  String FULL_TYPE_MATCHING = "fullTypeMatching";

  /**
   * @see ConverterConfiguration#isFullTypeMatching()
   */
  boolean fullTypeMatching();

  String INCLUDING_SUPER = "includingSuper";

  /**
   * @see ConverterConfiguration#isIncludingSuper()
   */
  boolean includingSuper();


}
