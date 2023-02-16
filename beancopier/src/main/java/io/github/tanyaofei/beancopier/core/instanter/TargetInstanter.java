package io.github.tanyaofei.beancopier.core.instanter;

/**
 * A tool for generating bytecode to instantiate the target
 *
 * @author tanyaofei
 */
public interface TargetInstanter {

  /**
   * Instantiate a target and complete fields assignment
   */
  void instantiate();

}
