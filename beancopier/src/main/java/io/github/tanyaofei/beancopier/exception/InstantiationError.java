package io.github.tanyaofei.beancopier.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Converter New Instance Exception, which is usually caused by internal reason.
 * You can submit an issue on GitHub to help use resolve this problem.
 *
 * @author tanyaofei
 * @since 0.1.2
 */
public class InstantiationError extends CodeError {

  @Internal
  public InstantiationError(@NotNull Class<?> c, @Nullable Throwable cause) {
    super("Failed to instantiate converter: " + c, cause);
  }

}
