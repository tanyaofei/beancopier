package io.github.tanyaofei.beancopier.constants;

import io.github.tanyaofei.beancopier.core.instanter.AllArgsConstructorInstanter;
import io.github.tanyaofei.beancopier.core.instanter.NoArgsConstructorInstanter;

/**
 * @author tanyaofei
 */
public enum InstantiateMode {

  /**
   * Instantiate a target using all-args-constructor
   * <p>Applicable in the following two cases：</p>
   * <ul>
   *   <li>{@link Record} class
   *   <li>No inheritance from a super class (or only from the {@link Object}),
   *   and a constructor that includes all parameters has been provided,
   *   with the same parameters order as the field order,
   *   and method parameters names included at compilation time</li>
   * </ul>
   *
   * @see AllArgsConstructorInstanter
   */
  ALL_ARGS_CONSTRUCTOR,

  /**
   * Instantiate a target using no-args-constructor, then copied fields using setters
   * @see NoArgsConstructorInstanter
   */
  NO_ARGS_CONSTRUCTOR,

}
