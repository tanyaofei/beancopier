package io.github.tanyaofei.beancopier.exception;

/**
 * 转换器创建实例异常, 这个异常通常是内部异常, 你可以在 github 提 issue 进行解决
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
