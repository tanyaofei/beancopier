package io.github.tanyaofei.beancopier.exception;

/**
 * {@link io.github.tanyaofei.beancopier.BeanCopier} 拷贝异常父类
 *
 * @see ConverterGenerateException
 * @see CopyException
 * @see ConverterNewInstanceException
 * @author tanyaofei
 * @since 0.0.1
 */
public class BeanCopierException extends RuntimeException {

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
