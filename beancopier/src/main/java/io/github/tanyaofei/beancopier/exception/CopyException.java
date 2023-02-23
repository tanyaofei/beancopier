package io.github.tanyaofei.beancopier.exception;
import static org.jetbrains.annotations.ApiStatus.Internal;
/**
 * Copy Exception. Any exceptions occur during copying will be wrapped as a CopyException.
 * <pre>{@code
 * try {
 *   List<Target> targets = BeanCopier.copyList(
 *    sources,
 *    (s, t) -> thrown new NullPointerException();
 * } catch (CopyException e) {
 *    assertEquals(e.getCause().getClass(), NullPointerException.class);
 * }
 * }</pre>
 *
 * @author tanyaofei
 * @since 0.1.2
 */
public non-sealed class CopyException extends BeanCopierException {

  @Internal
  public CopyException(String message, Throwable cause) {
    super(message, cause);
  }

}
