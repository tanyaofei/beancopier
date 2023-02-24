package io.github.tanyaofei.beancopier.core.annotation;

import io.github.tanyaofei.beancopier.ConverterFeature;

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
   * @see ConverterFeature#isSkipNull()
   */
  boolean skipNull();

  String PREFER_NESTED = "preferNested";

  /**
   * @see ConverterFeature#isPreferNested() ()
   */
  boolean preferNested();

  String PROPERTY_SUPPORTED = "propertySupported";

  /**
   * @see ConverterFeature#isPropertySupported()
   */
  boolean propertySupported();

  String FULL_TYPE_MATCHING = "fullTypeMatching";

  /**
   * @see ConverterFeature#isFullTypeMatching()
   */
  boolean fullTypeMatching();

  String INCLUDING_SUPER = "includingSuper";

  /**
   * @see ConverterFeature#isIncludingSuper()
   */
  boolean includingSuper();


}
