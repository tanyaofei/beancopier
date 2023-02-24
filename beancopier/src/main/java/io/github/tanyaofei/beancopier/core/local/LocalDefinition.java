package io.github.tanyaofei.beancopier.core.local;

import io.github.tanyaofei.beancopier.utils.GenericType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nonnull;

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
  @Nonnull
  private final String name;

  /**
   * The generic type of the filed required
   */
  @Nonnull
  private final GenericType<?> type;

  /**
   * Whether this local variable should be skipped
   */
  private final boolean skip;

}
