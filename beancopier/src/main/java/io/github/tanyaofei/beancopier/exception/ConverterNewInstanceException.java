package io.github.tanyaofei.beancopier.exception;

/**
 * Converter New Instance Exception, which is usually caused by internal reason.
 * You can submit an issue on GitHub to help use resolve this problem.
 *
 * @author tanyaofei
 * @since 0.1.2
 */
public final class ConverterNewInstanceException extends BeanCopierException {

  public ConverterNewInstanceException(Throwable cause) {
    super(cause);
  }

  public ConverterNewInstanceException(String message) {
    super(message);
  }

  public ConverterNewInstanceException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConverterNewInstanceException(Class<?> c, Throwable cause) {
    super("Failed to new instance of converter: " + c, cause);
  }

}
