package io.github.tanyaofei.beancopier.exception;

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
public final class CopyException extends BeanCopierException {

  public CopyException(Throwable cause) {
    super(cause);
  }

  public CopyException(String message) {
    super(message);
  }

  public CopyException(String message, Throwable cause) {
    super(message, cause);
  }

}
