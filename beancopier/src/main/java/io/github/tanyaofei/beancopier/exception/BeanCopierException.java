package io.github.tanyaofei.beancopier.exception;

import io.github.tanyaofei.beancopier.BeanCopier;

/**
 * {@link BeanCopier} 拷贝异常父类
 *
 * @author tanyaofei
 * @see ConverterGenerateException
 * @see CopyException
 * @see ConverterNewInstanceException
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
