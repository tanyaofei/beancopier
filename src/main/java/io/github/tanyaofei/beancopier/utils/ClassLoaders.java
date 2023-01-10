package io.github.tanyaofei.beancopier.utils;

public class ClassLoaders {

  /**
   * 判断 superClassLoader 是否是 subClassLoader 的祖宗
   *
   * @param superClassLoader 可能为祖宗的 classloader
   * @param subClassLoader   可能为子孙的 classloader
   * @return true 表示 superClassLoader 是 subClassLoader, 反则为 false
   */
  public static boolean isAssignableFrom(ClassLoader superClassLoader, ClassLoader subClassLoader) {
    if (superClassLoader == subClassLoader) {
      return true;
    }

    ClassLoader p = subClassLoader;
    while (p.getParent() != null) {
      if (p == superClassLoader) {
        return true;
      }
      p = p.getParent();
    }

    return false;
  }

}
