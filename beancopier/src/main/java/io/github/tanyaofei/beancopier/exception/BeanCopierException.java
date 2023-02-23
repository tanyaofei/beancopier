package io.github.tanyaofei.beancopier.exception;

import static org.jetbrains.annotations.ApiStatus.Internal;

/**
 * <b>beancopier</b> exception superclass
 *
 * @author tanyaofei
 * @since 0.0.1
 */
public sealed class BeanCopierException extends RuntimeException permits VerifyException, CopyException {

  @Internal
  public BeanCopierException(Throwable cause) {
    super(cause);
  }

  @Internal
  public BeanCopierException(String message) {
    super(message);
  }

  @Internal
  public BeanCopierException(String message, Throwable cause) {
    super(message, cause);
  }

}
