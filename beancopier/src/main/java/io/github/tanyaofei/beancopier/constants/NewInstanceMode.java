package io.github.tanyaofei.beancopier.constants;

/**
 * @author tanyaofei
 */
public enum NewInstanceMode {

  /**
   * 直接通过包含所有参数的构造器进行实例
   * <p>适用于以下两种情况：</p>
   * <ul>
   *   <li>{@link Record} 类
   *   <li>没有继承父类(或者只继承了 {@link Object} 类并且提供了一个包含所有参数的构造器，同时该构造器的参数顺序与字段顺序一致并且在编译时包含了方法参数名称时可用</li>
   * </ul>
   */
  ALL_ARGS_CONSTRUCTOR,

  /**
   * 先通过无参构造函数实例化再进行 get/set
   */
  NO_ARGS_CONSTRUCTOR_THEN_GET_SET,

}
