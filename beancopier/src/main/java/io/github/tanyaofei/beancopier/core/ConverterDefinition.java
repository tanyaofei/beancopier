package io.github.tanyaofei.beancopier.core;

import io.github.tanyaofei.beancopier.ConverterFeature;
import io.github.tanyaofei.beancopier.annotation.Property;
import io.github.tanyaofei.beancopier.constants.InstantiateMode;
import io.github.tanyaofei.beancopier.converter.Converter;
import lombok.Data;
import lombok.experimental.Accessors;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;

@Data
@Accessors(chain = true)
public class ConverterDefinition {

  /**
   * The internal of converter class
   */
  @Nonnull
  private final String internalName;

  /**
   * The class of source
   */
  @Nonnull
  private final Class<?> sourceType;

  /**
   * The class of target
   */
  @Nonnull
  private final Class<?> targetType;

  /**
   * The method descriptor of {@link Converter#convert(Object)} implementation
   */
  @Nonnull
  private final String convertMethodDescriptor;

  /**
   * The features of the converter that is about to be generated
   */
  @Nonnull
  private final ConverterFeature feature;

  /**
   * Whether it is a clone copy. {@code true} if {@link #sourceType} is equals to {@link #targetType}
   *
   * @see Property#value()
   */
  @Nonnull
  private final boolean clone;

  /**
   * The instantiate mode of target
   */
  private final InstantiateMode instantiateMode;

  public ConverterDefinition(
      @Nonnull String internalName,
      @Nonnull Class<?> sourceType,
      @Nonnull Class<?> targetType,
      @Nonnull ConverterFeature feature,
      @Nonnull InstantiateMode instantiateMode
  ) {
    this.internalName = internalName;
    this.sourceType = sourceType;
    this.targetType = targetType;
    this.feature = feature;
    this.convertMethodDescriptor = Type.getMethodDescriptor(Type.getType(targetType), Type.getType(sourceType));
    this.clone = sourceType.equals(targetType);
    this.instantiateMode = instantiateMode;
  }

}
