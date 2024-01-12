package io.github.tanyaofei.beancopier.core.annotation;

import io.github.tanyaofei.beancopier.ConverterFeatures;

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
public @interface Feature {

  String SKIP_NULL          = "skipNull";
  String PREFER_NESTED      = "preferNested";
  String FULL_TYPE_MATCHING = "fullTypeMatching";
  String INCLUDING_SUPER    = "includingSuper";
  String PROPERTY_SUPPORTED = "propertySupported";

  /**
   * @see ConverterFeatures#isSkipNull()
   */
  boolean skipNull();

  /**
   * @see ConverterFeatures#isPreferNested() ()
   */
  boolean preferNested();

  /**
   * @see ConverterFeatures#isPropertySupported()
   */
  boolean propertySupported();

  /**
   * @see ConverterFeatures#isFullTypeMatching()
   */
  boolean fullTypeMatching();

  /**
   * @see ConverterFeatures#isIncludingSuper()
   */
  boolean includingSuper();


}
