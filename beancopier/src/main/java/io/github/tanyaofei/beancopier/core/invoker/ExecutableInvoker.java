package io.github.tanyaofei.beancopier.core.invoker;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * A tool for generating bytecode to execute an executable.
 *
 * @author tanyaofei
 * @see java.lang.reflect.Executable
 * @since 0.2.0
 */
public interface ExecutableInvoker {

  /**
   * Return a MethodInvoker that used to invoke the specified method
   *
   * @param method 方法
   * @return 方法调用字节码编写工具
   * @since 0.2.0
   */
  static MethodInvoker invoker(Method method) {
    return new MethodInvoker(method);
  }

  /**
   * Return a MethodInvoker that used to invoke the specified constructor
   *
   * @param constructor 构造器
   * @return 构造器调用字节码编写工具
   * @since 0.2.0
   */
  static ConstructorInvoker invoker(Constructor<?> constructor) {
    return new ConstructorInvoker(constructor);
  }

  /**
   * Write bytecode that invoking the executable, and keep the return value in stack if the executable return one
   *
   * @param v Method writer
   * @since 0.2.0
   */
  default void invoke(@NotNull MethodVisitor v) {
    invoke(v, false);
  }

  default void invoke(@NotNull MethodVisitor v, @NotNull Runnable beforeInvoke, @NotNull Runnable afterInvoke) {
    beforeInvoke.run();
    invoke(v);
    afterInvoke.run();
  }

  /**
   * Write bytecode that invoking the executable
   *
   * @param v              Method writer
   * @param popReturnValue Pop the return value from stack if the executable return one
   * @since 0.2.0
   */
  void invoke(@NotNull MethodVisitor v, boolean popReturnValue);

}
