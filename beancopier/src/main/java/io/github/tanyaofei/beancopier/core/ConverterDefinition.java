package io.github.tanyaofei.beancopier.core;

import io.github.tanyaofei.beancopier.ConverterConfiguration;
import io.github.tanyaofei.beancopier.annotation.Property;
import io.github.tanyaofei.beancopier.constants.InstantiateMode;
import io.github.tanyaofei.beancopier.converter.Converter;
import lombok.Data;
import lombok.experimental.Accessors;
import org.objectweb.asm.Type;

@Data
@Accessors(chain = true)
public class ConverterDefinition {

  /**
   * The internal of converter class
   */
  private final String internalName;

  /**
   * The class of source
   */
  private final Class<?> sourceType;

  /**
   * The class of target
   */
  private final Class<?> targetType;

  /**
   * The method descriptor of {@link Converter#convert(Object)} implementer
   */
  private final String convertMethodDescriptor;

  /**
   * The configuration of the converter that is about to be generated
   */
  private final ConverterConfiguration configuration;

  /**
   * Whether it is a clone copy. {@code true} if {@link #sourceType} is equals to {@link #targetType}
   *
   * @see Property#value()
   */
  private final boolean clone;

  /**
   * The instantiate mode of target
   */
  private final InstantiateMode instantiateMode;

  public ConverterDefinition(
      String internalName,
      Class<?> sourceType,
      Class<?> targetType,
      ConverterConfiguration configuration,
      InstantiateMode instantiateMode
  ) {
    this.internalName = internalName;
    this.sourceType = sourceType;
    this.targetType = targetType;
    this.configuration = configuration;
    this.convertMethodDescriptor = Type.getMethodDescriptor(Type.getType(targetType), Type.getType(sourceType));
    this.clone = sourceType.equals(targetType);
    this.instantiateMode = instantiateMode;
  }

}
