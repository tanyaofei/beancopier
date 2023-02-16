package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.converter.Converter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Converter classes naming policy
 *
 * @author tanyaofei
 * @see ConverterConfiguration#getNamingPolicy()
 * @since 0.1.2
 */
public abstract class NamingPolicy {

  /**
   * The default package for converter classes are about to be generated at runtime
   */
  private static final String DEFAULT_PACKAGE = Converter.class.getPackageName();

  public static Default getDefault() {
    return Default.INSTANCE;
  }

  /**
   * @return Return a package for generator is about to be generated
   */
  public String getPackage() {
    return DEFAULT_PACKAGE;
  }

  /**
   * @param sourceType The type of Source
   * @param targetType The type of Target
   * @param predicate  To predicate the name is used or not
   * @return A class name for generator is about to be generated
   */
  @NotNull
  public abstract String getClassName(
      @NotNull Class<?> sourceType,
      @NotNull Class<?> targetType,
      @NotNull Predicate<String> predicate
  );

  public static class Default extends NamingPolicy {

    private static final Default INSTANCE = new Default();

    @Override
    @NotNull
    @SuppressWarnings("StatementWithEmptyBody")
    public String getClassName(
        @NotNull Class<?> sourceType,
        @NotNull Class<?> targetType,
        @NotNull Predicate<String> predicate
    ) {
      var base = getPackage()
          + "."
          + sourceType.getSimpleName()
          + "To"
          + targetType.getSimpleName()
          + "Converter$$GeneratedByBeanCopier$$"
          + Integer.toHexString((sourceType.getName() + targetType.getName()).hashCode());

      var attempt = base;
      for (int i = 2; predicate.test(attempt); attempt = base + "_" + i++) {

      }
      return attempt;
    }
  }


}
