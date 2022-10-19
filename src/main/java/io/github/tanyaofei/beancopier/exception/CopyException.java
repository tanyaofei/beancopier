package io.github.tanyaofei.beancopier.exception;

/**
 * @author tanyaofei
 *  2021.07.0
 */
public class CopyException extends RuntimeException {

  public CopyException(String message) {
    super(message);
  }

  public CopyException(String message, Throwable cause) {
    super(message, cause);
  }

}
