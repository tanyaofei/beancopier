package io.github.tanyaofei.beancopier.core.local;

import io.github.tanyaofei.beancopier.utils.reflection.member.BeanMember;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * The context of locals definition
 */
@Data
@Accessors(chain = true)
public class LocalsDefinitionContext {

  /**
   * The bean members map of source
   */
  private Map<String, BeanMember> sourceMembers;

  /**
   * The index where the next local variable will be stored.
   * This field is maintained because some variables occupy two slots such as long, double.
   */
  int nextStore;

}
