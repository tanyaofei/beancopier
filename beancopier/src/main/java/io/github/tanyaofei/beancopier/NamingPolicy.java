package io.github.tanyaofei.beancopier;

import org.jetbrains.annotations.NotNull;

/**
 * Converter classes naming policy
 *
 * @author tanyaofei
 * @see ConverterConfiguration#getNamingPolicy()
 * @since 0.1.2
 */
public abstract class NamingPolicy {

  public static Default getDefault() {
    return Default.INSTANCE;
  }

  /**
   * @param sourceType The type of Source
   * @param targetType The type of Target
   * @return A class name for generator is about to be generated
   */
  @NotNull
  public abstract String getSimpleClassName(
      @NotNull Class<?> sourceType,
      @NotNull Class<?> targetType
  );

  public static class Default extends NamingPolicy {

    private static final Default INSTANCE = new Default();

    @Override
    @NotNull
    public String getSimpleClassName(
        @NotNull Class<?> sourceType,
        @NotNull Class<?> targetType
    ) {
      if (sourceType == targetType) {
        return sourceType.getSimpleName()
            + "CloningConverter";
      } else {
        return sourceType.getSimpleName()
            + "To"
            + targetType.getSimpleName()
            + "Converter";
      }

    }
  }


}
