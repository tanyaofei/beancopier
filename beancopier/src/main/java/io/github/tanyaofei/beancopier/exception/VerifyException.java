package io.github.tanyaofei.beancopier.exception;

import org.jetbrains.annotations.Nullable;

import static org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Converter generate exception, which is usually caused by internal reason.
 * You can submit an issue on GitHub to help use resolve this problem.
 *
 * @author tanyaofei
 * @since 0.1.2
 */
public class VerifyException extends BeanCopierException {


  @Internal
  public VerifyException(@Nullable String message) {
    super(message);
  }

  @Internal
  public VerifyException(@Nullable String message, @Nullable Throwable cause) {
    super(message, cause);
  }

}
