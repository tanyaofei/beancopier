package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.annotation.Property;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;

/**
 * Converter generation configuration
 *
 * @author tanyaofei
 * @since 0.1.5
 */
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConverterFeature {

  /**
   * Determines whether Target field must define a full match in order to be applied.
   */
  private final boolean fullTypeMatching;

  /**
   * Determines whether a property should be skipped or not when the property value is null
   * <ul>
   *   <li>This feature does not take effect when copying to a {@link Record}.</li>
   *   <li>This feature does not take effect when copying primitive fields such as {@code int}, {@code long}...</li>
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
   * Determines a lookup for defining the generated converter class.
   * <br>
   * In java, beancopier can not access classes loaded by other classloaders,
   * so we need to provide a {@link java.lang.invoke.MethodHandles.Lookup} that can access those classes in order to access them.
   * Beancopier provides a way to obtain a {@link java.lang.invoke.MethodHandles.Lookup} for a specified classloader.
   * <pre>{@code
   * OtherClassLoader cl = (OtherClassLoader)target.getClass().getClassLoader();
   * MethodHandlers.Lookup lookup = LookupUtils.lookupInModule(cl, OtherClassLoader::defineClass);
   * BeanCopierImpl copier = new BeanCopierImpl(feature -> feature.lookup(lookup));
   * }</pre>
   */
  private final MethodHandles.Lookup lookup;

  /**
   * Determines a naming policy for naming the generated converter class
   */
  private final NamingPolicy namingPolicy;

  /**
   * When generating the converter, write the class file to the specified location.
   */
  private final String debugLocation;

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private boolean fullTypeMatching = false;

    private boolean skipNull = false;

    private boolean preferNested = true;

    private boolean includingSuper = true;

    private boolean propertySupported = true;

    private MethodHandles.Lookup lookup = null;

    private NamingPolicy namingPolicy = NamingPolicy.getDefault();

    private String debugLocation = new BeanCopierConfiguration().getDebugLocation();

    public ConverterFeature build() {
      return new ConverterFeature(
          fullTypeMatching,
          skipNull,
          preferNested,
          includingSuper,
          propertySupported,
          lookup,
          namingPolicy,
          debugLocation
      );
    }

    @Nonnull
    public Builder fullTypeMatching(boolean fullTypeMatching) {
      this.fullTypeMatching = fullTypeMatching;
      return this;
    }

    @Nonnull
    public Builder skipNull(boolean skipNull) {
      this.skipNull = skipNull;
      return this;
    }

    @Nonnull
    public Builder preferNested(boolean preferNested) {
      this.preferNested = preferNested;
      return this;
    }

    @Nonnull
    public Builder includingSuper(boolean includingSuper) {
      this.includingSuper = includingSuper;
      return this;
    }

    @Nonnull
    public Builder propertySupported(boolean propertySupported) {
      this.propertySupported = propertySupported;
      return this;
    }

    @Nonnull
    public Builder lookup(@Nullable MethodHandles.Lookup lookup) {
      this.lookup = lookup;
      return this;
    }

    @Nonnull
    public Builder namingPolicy(@NotNull NamingPolicy namingPolicy) {
      this.namingPolicy = namingPolicy;
      return this;
    }

    @Nonnull
    public Builder debugLocation(@Nullable String classDumpPath) {
      this.debugLocation = classDumpPath;
      return this;
    }
  }

}
