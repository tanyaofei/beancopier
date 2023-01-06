package io.github.tanyaofei.beancopier.exception;

public class CopyException extends BeanCopierException {


  public CopyException(String message) {
    super(message);
  }

  public CopyException(String message, Throwable cause) {
    super(message, cause);
  }

}
