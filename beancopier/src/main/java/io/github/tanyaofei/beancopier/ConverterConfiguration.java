package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.annotation.Property;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Converter generation configuration
 *
 * @author tanyaofei
 * @since 0.1.5
 */
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConverterConfiguration {

  /**
   * Determines whether Target field must define a full match in order to be applied.
   */
  private final boolean fullTypeMatching;

  /**
   * Determines whether a property should be skipped or not when the property value is null
   * <ul>
   *   <li>This configuration does not take effect when copying to a {@link Record}.</ul>
   *   <li>This configuration does not take effect when copying primitive fields such as int, long, short...</ul>
   * </ul>
   */
  private final boolean skipNull;

  /**
   * Determines if the implicit mapping should map the nested properties,
   * we strongly recommend to disable this option while you are mapping a model contains circular reference
   */
  private final boolean preferNested;

  /**
   * Determines whether to copy fields from the superclass.
   */
  private final boolean includingSuper;

  /**
   * Determines whether to enabled {@link Property}
   */
  private final boolean propertySupported;

  /**
   * Determines a classloader for load the generated converter class
   */
  private final ClassLoader classLoader;

  /**
   * Determines a naming policy for naming the generated converter class
   */
  private final NamingPolicy namingPolicy;

  /**
   * When generating the converter, write the class file to the specified location.
   * <p>If this configuration is null, {@link BeanCopierConfiguration#CONVERTER_CLASS_DUMP_PATH} will be used as default</p>
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
