package io.github.tanyaofei.beancopier;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Converter classes naming policy
 *
 * @author tanyaofei
 * @since 0.1.2
 */
public abstract class NamingPolicy {

  public static Default getDefault() {
    return Default.INSTANCE;
  }


  /**
   * @param sourceType The type of Source
   * @param targetType The type of Target
   * @param predicate  To predicate the name is used or not
   * @return A class name for generator is about to be generated
   */
  @NotNull
  public abstract String getClassName(
      @NotNull String packageName,
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
        @NotNull String packageName,
        @NotNull Class<?> sourceType,
        @NotNull Class<?> targetType,
        @NotNull Predicate<String> predicate
    ) {
      var base = packageName + "." + sourceType.getSimpleName()
          + "To"
          + targetType.getSimpleName()
          + "Converter$$"
          + Integer.toHexString((sourceType.getName() + targetType.getName()).hashCode());

      String attempt = base;
      for (int i = 2; predicate.test(attempt); attempt = base + "_" + i++) {

      }
      return attempt;
    }
  }


}