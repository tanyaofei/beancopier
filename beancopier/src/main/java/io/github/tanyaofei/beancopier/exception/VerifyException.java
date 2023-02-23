package io.github.tanyaofei.beancopier.exception;

import static org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Converter generate exception, which is usually caused by internal reason.
 * You can submit an issue on GitHub to help use resolve this problem.
 *
 * @author tanyaofei
 * @since 0.1.2
 */
public non-sealed class VerifyException extends BeanCopierException {


  @Internal
  public VerifyException(String message) {
    super(message);
  }

  @Internal
  public VerifyException(String message, Throwable cause) {
    super(message, cause);
  }

}
