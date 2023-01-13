package io.github.tanyaofei.beancopier;


/**
 * BeanCopier 全局配置
 *
 * @author tanyaofei
 * @since 0.0.8
 */
public abstract class BeanCopierConfiguration {

  /**
   * 生成转换器时将 class 文件写入指定位置
   *
   * @since 0.0.8
   */
  public static String CONVERTER_CLASS_DUMP_PATH = System.getProperty(PropertyNames.CONVERTER_CLASS_DUMP_PATH, "");

  /**
   * BeanCopier 配置名称
   *
   * @since 0.0.8
   */
  public interface PropertyNames {
    String CONVERTER_CLASS_DUMP_PATH = "io.github.tanyaofei.beancopier.converterClassDumpPath";
  }


}
