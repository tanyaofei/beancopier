package io.github.tanyaofei.beancopier.exception;
import static org.jetbrains.annotations.ApiStatus.Internal;
/**
 * Converter New Instance Exception, which is usually caused by internal reason.
 * You can submit an issue on GitHub to help use resolve this problem.
 *
 * @author tanyaofei
 * @since 0.1.2
 */
public non-sealed class InstantiationError extends CodeError {

  @Internal
  public InstantiationError(Class<?> c, Throwable cause) {
    super("Failed to instantiate converter: " + c, cause);
  }

}
