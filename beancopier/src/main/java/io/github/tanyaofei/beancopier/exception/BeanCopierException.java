package io.github.tanyaofei.beancopier.exception;

import org.jetbrains.annotations.Nullable;

import static org.jetbrains.annotations.ApiStatus.Internal;

/**
 * <b>beancopier</b> exception superclass
 *
 * @author tanyaofei
 * @since 0.0.1
 */
public sealed class BeanCopierException extends RuntimeException permits VerifyException, CopyException {

  @Internal
  public BeanCopierException(@Nullable Throwable cause) {
    super(cause);
  }

  @Internal
  public BeanCopierException(@Nullable String message) {
    super(message);
  }

  @Internal
  public BeanCopierException(@Nullable String message, @Nullable Throwable cause) {
    super(message, cause);
  }

}
