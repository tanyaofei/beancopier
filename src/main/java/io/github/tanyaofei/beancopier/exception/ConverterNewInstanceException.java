package io.github.tanyaofei.beancopier.exception;

/**
 * 转换器创建实例异常, 这个异常通常是内部异常, 你可以在 github 提 issue 进行解决
 */
public class ConverterNewInstanceException extends BeanCopierException {

  public ConverterNewInstanceException(String message) {
    super(message);
  }

  public ConverterNewInstanceException(String message, Throwable cause) {
    super(message, cause);
  }

}
