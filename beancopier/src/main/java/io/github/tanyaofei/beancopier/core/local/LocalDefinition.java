package io.github.tanyaofei.beancopier.core.local;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Type;

/**
 * Local variable definition
 *
 * @author tanyaofei
 * @since 0.2.0
 */
@Builder
@Getter
@ToString
public class LocalDefinition {

  /**
   * The name of the field to be copied.
   */
  private final String name;

  /**
   * The generic type of this local variable
   */
  private final Type genericType;

  /**
   * The class of this local variable
   */
  private final Class<?> type;

  /**
   * Whether this local variable should be skipped
   */
  private final boolean skip;

}
