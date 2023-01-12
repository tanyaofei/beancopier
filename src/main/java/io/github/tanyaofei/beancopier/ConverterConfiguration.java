package io.github.tanyaofei.beancopier;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 转换器配置
 *
 * @author tanyaofei
 * @since 0.1.5
 */
@Getter
@Builder
@ToString
public class ConverterConfiguration {

  /**
   * 是否完整类型匹配, 如果为 ture 则当字段的类型完全一致才会拷贝，否则类型兼容就可以拷贝
   */
  @Builder.Default
  private boolean fullTypeMatching = false;

  /**
   * 是否跳过 null 字段。如果为 true 则在拷贝字段前先判断是否为 null，否则会将 null 拷贝到目标字段
   */
  @Builder.Default
  private boolean skipNull = false;

  /**
   * 是否嵌套拷贝字段，如果拷贝的对象可能出现循环字段，那程序就会出现死循环
   */
  @Builder.Default
  private boolean preferNested = true;

  /**
   * 是否拷贝父类字段
   */
  @Builder.Default
  private boolean includingSuper = true;

  /**
   * 是否引入对 {@link io.github.tanyaofei.beancopier.annotation.Property} 注解的支持
   */
  @Builder.Default
  private boolean propertySupported = true;

  /**
   * 指定使用特定的类加载器对生成的 Converter 进行加载
   */
  @Builder.Default
  private ClassLoader classLoader = null;

  /**
   * 转换器的命名策略
   */
  @Builder.Default
  private NamingPolicy namingPolicy = NamingPolicy.getDefault();

  /**
   * 生成的类导出路径, 如果为 null 或者为空则表示不导出，在使用这个功能的时候要确保指定目录已存在，导出失败时不会抛出任何异常
   */
  @Builder.Default
  private String classDumpPath = BeanCopierConfiguration.CONVERTER_CLASS_DUMP_PATH;

}
