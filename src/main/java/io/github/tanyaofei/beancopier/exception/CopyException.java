package io.github.tanyaofei.beancopier.exception;

/**
 * 拷贝异常
 *
 * @author tanyaofei
 * @since 0.1.2
 */
public class CopyException extends BeanCopierException {

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
