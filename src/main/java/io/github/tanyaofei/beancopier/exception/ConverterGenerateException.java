package io.github.tanyaofei.beancopier.exception;


/**
 * 转换器生成异常, 这个异常通常是内部异常, 你可以在 github 提 issue 进行解决
 *
 * @author tanyaofei
 * @since 0.1.2
 */
public class ConverterGenerateException extends BeanCopierException {

  public ConverterGenerateException(Throwable cause) {
    super(cause);
  }

  public ConverterGenerateException(String message) {
    super(message);
  }

  public ConverterGenerateException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConverterGenerateException(Class<?> sc, Class<?> tc, Throwable cause) {
    super("Failed to generate converter class: " + sc.getName() + " -> " + tc.getName(), cause);
  }

}
