package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.converter.Converter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * 转换器类名命名策略
 *
 * @author tanyaofei
 * @since 0.1.2
 */
public abstract class NamingPolicy {

  private static final String DEFAULT_PACKAGE = Converter.class.getPackageName();

  public static Default getDefault() {
    return Default.INSTANCE;
  }

  public String getPackage() {
    return DEFAULT_PACKAGE;
  }

  @NotNull
  public abstract String getClassName(
      @NotNull Class<?> sourceClass,
      @NotNull Class<?> targetClass,
      @NotNull Predicate<String> predicate
  );

  public static class Default extends NamingPolicy {

    private static final Default INSTANCE = new Default();

    @Override
    @NotNull
    @SuppressWarnings("StatementWithEmptyBody")
    public String getClassName(
        @NotNull Class<?> sourceClass,
        @NotNull Class<?> targetClass,
        @NotNull Predicate<String> predicate
    ) {
      String base = getPackage()
          + "."
          + sourceClass.getSimpleName()
          + "To"
          + targetClass.getSimpleName()
          + "Converter$$GeneratedByBeanCopier$$"
          + Integer.toHexString((sourceClass.getName() + targetClass.getName()).hashCode());

      String attempt = base;
      for (int i = 2; predicate.test(attempt); attempt = base + "_" + i++) {

      }
      return attempt;
    }
  }


}
