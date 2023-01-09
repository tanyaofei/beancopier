package io.github.tanyaofei.beancopier.exception;


/**
 * 转换器生成异常, 这个异常通常是内部异常, 你可以在 github 提 issue 进行解决
 */
public class ConverterGenerateException extends BeanCopierException {
  public ConverterGenerateException(String message) {
    super(message);
  }

  public ConverterGenerateException(String message, Throwable cause) {
    super(message, cause);
  }
}
