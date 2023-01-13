package io.github.tanyaofei.beancopier.core.local;

import io.github.tanyaofei.beancopier.annotation.Property;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Type;

/**
 * 局部变量定义
 *
 * @author tanyaofei
 * @since 0.2.0
 */
@Builder
@Getter
@ToString
public class LocalDefinition {

  /**
   * 字段名称
   * <p>如果使用了 {@link Property#value()} 为该值</p>
   */
  private final String name;

  /**
   * 字段类型
   */
  private final Type genericType;

  /**
   * 字段类
   */
  private final Class<?> type;

  /**
   * 是否跳过此字段
   */
  private final boolean skip;

}
