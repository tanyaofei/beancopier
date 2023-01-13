package io.github.tanyaofei.beancopier.core;

import io.github.tanyaofei.beancopier.ConverterConfiguration;
import io.github.tanyaofei.beancopier.constants.NewInstanceMode;
import io.github.tanyaofei.beancopier.converter.Converter;
import lombok.Data;
import lombok.experimental.Accessors;
import org.objectweb.asm.Type;

@Data
@Accessors(chain = true)
public class ConverterDefinition {

  /**
   * Converter 的内部名
   */
  private final String internalName;

  /**
   * 拷贝来源类
   */
  private final Class<?> sourceType;

  /**
   * 拷贝目标类
   */
  private final Class<?> targetType;

  /**
   * {@link Converter#convert(Object)} 实现类方法
   */
  private final String convertMethodDescriptor;

  /**
   * 转换器配置
   */
  private final ConverterConfiguration configuration;

  /**
   * 是否是克隆
   */
  private final boolean clone;

  /**
   * 实例化对象的方式
   */
  private final NewInstanceMode newInstanceMode;

  public ConverterDefinition(
      String internalName,
      Class<?> sourceType,
      Class<?> targetType,
      ConverterConfiguration configuration,
      NewInstanceMode newInstanceMode) {
    this.internalName = internalName;
    this.sourceType = sourceType;
    this.targetType = targetType;
    this.configuration = configuration;
    this.convertMethodDescriptor = Type.getMethodDescriptor(Type.getType(targetType), Type.getType(sourceType));
    this.clone = sourceType.equals(targetType);
    this.newInstanceMode = newInstanceMode;
  }

}
