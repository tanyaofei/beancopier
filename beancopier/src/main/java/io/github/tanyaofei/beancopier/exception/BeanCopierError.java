package io.github.tanyaofei.beancopier.exception;

import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * @author tanyaofei
 * @since 0.2.0
 */
public sealed class BeanCopierError extends Error permits CodeError {

  @Internal
  public BeanCopierError() {
    super();
  }

  @Internal
  public BeanCopierError(String message) {
    super(message);
  }

  @Internal
  public BeanCopierError(String message, Throwable cause) {
    super(message, cause);
  }

  @Internal
  public BeanCopierError(Throwable cause) {
    super(cause);
  }

}
