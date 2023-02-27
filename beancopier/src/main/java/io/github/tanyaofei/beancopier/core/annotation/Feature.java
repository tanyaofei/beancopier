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

  String SKIP_NULL = "skipNull";

  /**
   * @see ConverterFeatures#isSkipNull()
   */
  boolean skipNull();

  String PREFER_NESTED = "preferNested";

  /**
   * @see ConverterFeatures#isPreferNested() ()
   */
  boolean preferNested();

  String PROPERTY_SUPPORTED = "propertySupported";

  /**
   * @see ConverterFeatures#isPropertySupported()
   */
  boolean propertySupported();

  String FULL_TYPE_MATCHING = "fullTypeMatching";

  /**
   * @see ConverterFeatures#isFullTypeMatching()
   */
  boolean fullTypeMatching();

  String INCLUDING_SUPER = "includingSuper";

  /**
   * @see ConverterFeatures#isIncludingSuper()
   */
  boolean includingSuper();


}
