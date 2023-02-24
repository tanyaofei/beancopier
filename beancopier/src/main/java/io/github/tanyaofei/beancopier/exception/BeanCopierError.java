package io.github.tanyaofei.beancopier.exception;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

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
  public BeanCopierError(@Nullable String message) {
    super(message);
  }

  @Internal
  public BeanCopierError(@Nullable String message, @Nullable Throwable cause) {
    super(message, cause);
  }

  @Internal
  public BeanCopierError(@Nullable Throwable cause) {
    super(cause);
  }

}
