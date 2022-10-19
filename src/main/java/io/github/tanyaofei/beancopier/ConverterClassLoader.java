package io.github.tanyaofei.beancopier;

/**
 * @author tanyaofei
 */
public class ConverterClassLoader extends ClassLoader {

  public ConverterClassLoader(ClassLoader parent) {
    super(parent);
  }

  /**
   * 加载类
   *
   * @param name 类名
   * @param code 字节码字节数组
   * @return 类
   */
  public final Class<?> defineClass(String name, byte[] code) {
    return super.defineClass(name, code, 0, code.length);
  }

}
