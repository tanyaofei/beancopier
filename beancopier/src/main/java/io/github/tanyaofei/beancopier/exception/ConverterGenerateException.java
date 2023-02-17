package io.github.tanyaofei.beancopier.exception;


/**
 * Converter generate exception, which is usually caused by internal reason.
 * You can submit an issue on GitHub to help use resolve this problem.
 *
 * @author tanyaofei
 * @since 0.1.2
 */
public final class ConverterGenerateException extends BeanCopierException {

  public ConverterGenerateException(Throwable cause) {
    super(cause);
  }

  public ConverterGenerateException(String message) {
    super(message);
  }

  public ConverterGenerateException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConverterGenerateException(Class<?> sourceType, Class<?> targetType, Throwable cause) {
    super("Failed to generate converter class: " + sourceType.getName() + " -> " + targetType.getName(), cause);
  }

}
