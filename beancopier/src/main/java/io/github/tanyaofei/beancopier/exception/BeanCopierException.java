package io.github.tanyaofei.beancopier.exception;

/**
 * <b>beancopier</b> exception superclass
 *
 * @author tanyaofei
 * @since 0.0.1
 */
public sealed class BeanCopierException extends RuntimeException permits ConverterGenerateException, ConverterNewInstanceException, CopyException {

  public BeanCopierException(Throwable cause) {
    super(cause);
  }

  public BeanCopierException(String message) {
    super(message);
  }

  public BeanCopierException(String message, Throwable cause) {
    super(message, cause);
  }

}
