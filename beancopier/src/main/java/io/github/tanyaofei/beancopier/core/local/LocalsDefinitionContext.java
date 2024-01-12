package io.github.tanyaofei.beancopier.core.local;

import io.github.tanyaofei.beancopier.utils.reflection.member.BeanMember;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * The context of locals definition
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class LocalsDefinitionContext {

  /**
   * The bean members map of source
   */
  @NotNull
  private Map<String, BeanMember> providers;

  /**
   * The index where the next local variable will be stored.
   * This field is maintained because some variables occupy two slots such as long, double.
   */
  int nextStore;

}
