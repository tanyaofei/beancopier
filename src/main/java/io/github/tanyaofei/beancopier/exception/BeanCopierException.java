package io.github.tanyaofei.beancopier.exception;

/**
 * {@link io.github.tanyaofei.beancopier.BeanCopier} 拷贝异常父类
 *
 * @author tanyaofei
 * @see ConverterGenerateException
 * @see CopyException
 * @see ConverterNewInstanceException
 */
public class BeanCopierException extends RuntimeException {

  public BeanCopierException(String message) {
    super(message);
  }

  public BeanCopierException(String message, Throwable cause) {
    super(message, cause);
  }

}
