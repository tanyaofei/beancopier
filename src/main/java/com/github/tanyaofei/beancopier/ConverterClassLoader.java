package com.github.tanyaofei.beancopier;

/**
 * @author 谭耀飞
 * @since 2021.04.0
 */
public class ConverterClassLoader extends ClassLoader {

  public ConverterClassLoader(String name, ClassLoader parent) {
    super(name, parent);
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
