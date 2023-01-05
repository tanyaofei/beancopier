package io.github.tanyaofei.beancopier;

import java.util.function.Predicate;

public abstract class NamingPolicy {

  private static final String DEFAULT_PACKAGE = "io.github.tanyaofei.beancopier.converter";

  public static Default getDefault() {
    return Default.INSTANCE;
  }

  public String getPackage() {
    return DEFAULT_PACKAGE;
  }

  public abstract String getClassName(Class<?> sourceClass, Class<?> targetClass, Predicate<String> predicate);


  public static class Default extends NamingPolicy {

    private static final Default INSTANCE = new Default();

    @Override
    public String getClassName(Class<?> sourceClass, Class<?> targetClass, Predicate<String> predicate) {
      String base = getPackage()
          + "."
          + sourceClass.getSimpleName()
          + "to"
          + targetClass.getSimpleName()
          + "Converter$$GeneratedByBeanCopier$$"
          + Integer.toHexString((sourceClass.getName() + targetClass.getName()).hashCode());

      String attempt = base;
      for (int i = 2; predicate.test(attempt); attempt = base + "_" + i) {

      }
      return attempt;
    }
  }


}
