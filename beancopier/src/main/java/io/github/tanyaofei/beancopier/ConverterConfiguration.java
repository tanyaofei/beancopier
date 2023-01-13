package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.annotation.Property;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 转换器配置
 *
 * @author tanyaofei
 * @since 0.1.5
 */
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConverterConfiguration {

  /**
   * 是否完整类型匹配, 如果为 ture 则当字段的类型完全一致才会拷贝，否则类型兼容就可以拷贝
   */
  private final boolean fullTypeMatching;

  /**
   * 是否跳过 null 字段。如果为 true 则在拷贝字段前先判断是否为 null，否则会将 null 拷贝到目标字段
   * <p>注意：</p>
   * <ul>
   *   <li>该配置在拷贝到 {@link Record} 类时不生效</ul>
   *   <li>该配置在拷贝到原始类型时不生效如: int, boolean, char, byte, long, short... etc</ul>
   * </ul>
   */
  private final boolean skipNull;

  /**
   * 是否嵌套拷贝字段，如果拷贝的对象可能出现循环字段，那程序就会出现死循环
   */
  private final boolean preferNested;

  /**
   * 是否拷贝父类字段
   */
  private final boolean includingSuper;

  /**
   * 是否引入对 {@link Property} 注解的支持
   */
  private final boolean propertySupported;

  /**
   * 指定使用特定的类加载器对生成的 Converter 进行加载
   */
  private final ClassLoader classLoader;

  /**
   * 转换器的命名策略
   */
  private final NamingPolicy namingPolicy;

  /**
   * 生成的类导出路径, 如果为 null 或者为空则表示不导出，在使用这个功能的时候要确保指定目录已存在，导出失败时不会抛出任何异常
   */
  private final String classDumpPath;

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private boolean fullTypeMatching = false;

    private boolean skipNull = false;

    private boolean preferNested = true;

    private boolean includingSuper = true;

    private boolean propertySupported = true;

    private ClassLoader classLoader = null;

    private NamingPolicy namingPolicy = NamingPolicy.getDefault();

    private String classDumpPath = BeanCopierConfiguration.CONVERTER_CLASS_DUMP_PATH;

    public ConverterConfiguration build() {
      return new ConverterConfiguration(fullTypeMatching, skipNull, preferNested, includingSuper, propertySupported, classLoader, namingPolicy, classDumpPath);
    }

    public Builder fullTypeMatching(boolean fullTypeMatching) {
      this.fullTypeMatching = fullTypeMatching;
      return this;
    }

    public Builder skipNull(boolean skipNull) {
      this.skipNull = skipNull;
      return this;
    }

    public Builder preferNested(boolean preferNested) {
      this.preferNested = preferNested;
      return this;
    }

    public Builder includingSuper(boolean includingSuper) {
      this.includingSuper = includingSuper;
      return this;
    }

    public Builder propertySupported(boolean propertySupported) {
      this.propertySupported = propertySupported;
      return this;
    }

    public Builder classLoader(ClassLoader classLoader) {
      this.classLoader = classLoader;
      return this;
    }

    public Builder namingPolicy(NamingPolicy namingPolicy) {
      this.namingPolicy = namingPolicy;
      return this;
    }

    public Builder classDumpPath(String classDumpPath) {
      this.classDumpPath = classDumpPath;
      return this;
    }
  }

}
