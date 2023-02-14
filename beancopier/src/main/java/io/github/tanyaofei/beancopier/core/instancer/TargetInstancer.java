package io.github.tanyaofei.beancopier.core.instancer;

/**
 * A tool for generating bytecode to instantiate the target
 *
 * @author tanyaofei
 */
public interface TargetInstancer {

  /**
   * Instantiate a target and complete fields assignment
   */
  void instantiate();

}
