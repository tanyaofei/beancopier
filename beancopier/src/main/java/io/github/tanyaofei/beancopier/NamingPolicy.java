package io.github.tanyaofei.beancopier;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandles;

/**
 * Converter classes naming policy
 *
 * @author tanyaofei
 * @see ConverterFeature.Builder#namingPolicy(NamingPolicy)
 * @since 0.1.2
 */
public abstract class NamingPolicy {

  public static Default getDefault() {
    return Default.INSTANCE;
  }

  /**
   * Return a simple name of the converter class is about to be generated.
   * <br>
   * Not considering uniqueness because defining a hidden class will automatically rename the class names so that they won't conflict.
   *
   * @param sourceType The type of Source
   * @param targetType The type of Target
   * @return A class name for generator is about to be generated
   * @see java.lang.invoke.MethodHandles.Lookup#defineHiddenClass(byte[], boolean, MethodHandles.Lookup.ClassOption...) `
   * @throws NullPointerException if the return value is {@code null}
   */
  @NotNull
  public abstract String getSimpleClassName(
      @Nonnull Class<?> sourceType,
      @Nonnull Class<?> targetType
  );

  public static class Default extends NamingPolicy {

    private static final Default INSTANCE = new Default();

    @Override
    @NotNull
    public String getSimpleClassName(
        @Nonnull Class<?> sourceType,
        @Nonnull Class<?> targetType
    ) {
      if (sourceType == targetType) {
        return sourceType.getSimpleName()
            + "Copier";
      } else {
        return sourceType.getSimpleName()
            + "To"
            + targetType.getSimpleName()
            + "Converter";
      }

    }
  }


}
