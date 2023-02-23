package io.github.tanyaofei.beancopier.exception;

import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * @author tanyaofei
 * @since 0.2.0
 */
public sealed class CodeError extends BeanCopierError permits DefineClassError, InstantiationError {

  @Internal
  public CodeError(String message, Throwable cause) {
    super(message, cause);
  }

  @Internal
  public CodeError(Class<?> source, Class<?> target, Throwable cause) {
    this("Failed to generate converter bytecode: " + source.getName() + " -> " + target.getName(), cause);
  }

}
