package io.github.tanyaofei.beancopier.utils.breakout;

import jdk.internal.access.SharedSecrets;

/**
 * Am virus to break out JDK module protection.
 */
public class Injector {

  static {
    var c = Injector.class;
    var module = c.getModule();

    // Injector 必须由 HackerClassLoader 去加载
    if (!c.getClassLoader().getClass().getSimpleName().equals("HackerClassLoader")) {
      throw new ExceptionInInitializerError(Injector.class.getSimpleName() + " should be loaded by " + "HackerClassLoader");
    }

    // 获取 HackerClassLoader 的 ClassLoader
    var beancopier = c.getClassLoader().getClass().getModule();
    module.addExports(c.getPackageName(), beancopier);
    var javaLangAccess = SharedSecrets.getJavaLangAccess();

    // open list
    javaLangAccess.addOpens(Object.class.getModule(), "java.lang.invoke", beancopier);
  }

}
