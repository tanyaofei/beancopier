package io.github.tanyaofei.beancopier.constants;

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
   * @see io.github.tanyaofei.beancopier.core.instancer.AllArgsConstructorInstancer
   */
  ALL_ARGS_CONSTRUCTOR,

  /**
   * Instantiate a target using no-args-constructor, then copied fields using setters
   * @see io.github.tanyaofei.beancopier.core.instancer.NoArgsConstructorInstancer
   */
  NO_ARGS_CONSTRUCTOR_THEN_GET_SET,

}
