package io.github.tanyaofei.beancopier;


/**
 * Global configuration for <b>beancopier</b>
 *
 * @author tanyaofei
 * @since 0.0.8
 */
public abstract class BeanCopierConfiguration {

  /**
   * When generating the converter, write the class file to the specified location.
   *
   * @since 0.0.8
   */
  public static String CONVERTER_CLASS_DUMP_PATH = System.getProperty(PropertyNames.CONVERTER_CLASS_DUMP_PATH, "");

  /**
   * <b>beancopier</b> configuration names.
   *
   * @since 0.0.8
   */
  public interface PropertyNames {
    String CONVERTER_CLASS_DUMP_PATH = "io.github.tanyaofei.beancopier.converterClassDumpPath";
  }


}
