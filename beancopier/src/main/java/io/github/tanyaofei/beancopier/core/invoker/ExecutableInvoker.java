package io.github.tanyaofei.beancopier.core.invoker;

import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author tanyaofei
 * @since 0.2.0
 * 可执行字节码编写工具
 */
public interface ExecutableInvoker {

  /**
   * 创建一个方法调用字节码编写工具
   *
   * @param method 方法
   * @return 方法调用字节码编写工具
   * @since 0.2.0
   */
  static MethodInvoker invoker(Method method) {
    return new MethodInvoker(method);
  }

  /**
   * 创建一个构造器调用字节码编写工具
   *
   * @param constructor 构造器
   * @return 构造器调用字节码编写工具
   * @since 0.2.0
   */
  static ConstructorInvoker invoker(Constructor<?> constructor) {
    return new ConstructorInvoker(constructor);
  }

  /**
   * 编写调用字节码
   *
   * @param v 方法编写器
   * @since 0.2.0
   */
  default void invoke(MethodVisitor v) {
    invoke(v, false);
  }

  /**
   * 编写调用字节码
   *
   * @param v              方法编写器
   * @param popReturnValue 是否将调用后的返回值从栈中弹出
   * @since 0.2.0
   */
  void invoke(MethodVisitor v, boolean popReturnValue);

}
