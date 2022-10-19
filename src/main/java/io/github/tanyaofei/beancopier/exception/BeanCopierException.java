package io.github.tanyaofei.beancopier.exception;

/**
 * @author tanyaofei
 */
public class BeanCopierException extends RuntimeException {

  public BeanCopierException(String message) {
    super(message);
  }

  public BeanCopierException(String message, Throwable cause) {
    super(message, cause);
  }

}
