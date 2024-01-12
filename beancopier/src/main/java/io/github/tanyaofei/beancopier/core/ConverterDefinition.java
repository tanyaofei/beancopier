package io.github.tanyaofei.beancopier.core;

import io.github.tanyaofei.beancopier.ConverterFeatures;
import io.github.tanyaofei.beancopier.annotation.Property;
import io.github.tanyaofei.beancopier.constants.InstantiateMode;
import io.github.tanyaofei.beancopier.converter.Converter;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;


@Data
@Accessors(chain = true)
public class ConverterDefinition {

  /**
   * The internal of converter class
   */
  @NotNull
  private final String internalName;

  /**
   * The class of source
   */
  @NotNull
  private final Class<?> sourceType;

  /**
   * The class of target
   */
  @NotNull
  private final Class<?> targetType;

  /**
   * The method descriptor of {@link Converter#convert(Object)} implementation
   */
  @NotNull
  private final String convertMethodDescriptor;

  /**
   * The features of the converter that is about to be generated
   */
  @NotNull
  private final ConverterFeatures features;

  /**
   * Whether it is a clone copy. {@code true} if {@link #sourceType} is equals to {@link #targetType}
   *
   * @see Property#value()
   */
  @NotNull
  private final boolean clone;

  /**
   * The instantiate mode of target
   */
  private final InstantiateMode instantiateMode;

  public ConverterDefinition(
      @NotNull String internalName,
      @NotNull Class<?> sourceType,
      @NotNull Class<?> targetType,
      @NotNull ConverterFeatures features,
      @NotNull InstantiateMode instantiateMode
  ) {
    this.internalName = internalName;
    this.sourceType = sourceType;
    this.targetType = targetType;
    this.features = features;
    this.convertMethodDescriptor = Type.getMethodDescriptor(Type.getType(targetType), Type.getType(sourceType));
    this.clone = sourceType.equals(targetType);
    this.instantiateMode = instantiateMode;
  }

}
