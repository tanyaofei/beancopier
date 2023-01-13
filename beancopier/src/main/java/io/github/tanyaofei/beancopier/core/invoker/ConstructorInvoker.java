package io.github.tanyaofei.beancopier.core.invoker;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;

/**
 * 构造器调用字节码调用编写工具
 *
 * @author tanyaofei
 * @since 0.2.0
 */
public class ConstructorInvoker implements ExecutableInvoker {

  /**
   * 定义该构造器的类的内部名
   */
  private final String owner;

  /**
   * 构造器描述符
   */
  private final String descriptor;

  /**
   * @param constructor 构造器
   * @since 0.2.0
   */
  public ConstructorInvoker(Constructor<?> constructor) {
    this.owner = Type.getInternalName(constructor.getDeclaringClass());
    this.descriptor = Type.getConstructorDescriptor(constructor);
  }

  /**
   * 创建一个无参构造器调用字节码编写工具
   *
   * @param type 类
   * @return 构造器调用字节码编写工具
   * @throws IllegalArgumentException 如果该类不存在无参构造函数
   */
  public static ConstructorInvoker fromNoArgsConstructor(Class<?> type) {
    Constructor<?> constructor;
    try {
      constructor = type.getDeclaredConstructor();
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(type + " has not a no args constructor", e);
    }
    return new ConstructorInvoker(constructor);
  }

  @Override
  public void invoke(MethodVisitor v, boolean popReturnValue) {
    invoke(v, popReturnValue, null);
  }

  /**
   * 编写调用字节码
   *
   * @param v          方法编写器
   * @param beforeInit 执行 init 方法前做的事情
   */
  public void invoke(MethodVisitor v, Runnable beforeInit) {
    invoke(v, false, beforeInit);
  }

  /**
   * 编写调用字节码
   *
   * @param v              方法编写器
   * @param popReturnValue 是否将调用构造器后返回的变量从栈中弹出
   * @param beforeInit     执行 init 方法前做的事情
   */
  public void invoke(MethodVisitor v, boolean popReturnValue, Runnable beforeInit) {
    v.visitTypeInsn(Opcodes.NEW, owner);
    v.visitInsn(Opcodes.DUP);
    if (beforeInit != null) {
      beforeInit.run();
    }
    v.visitMethodInsn(Opcodes.INVOKESPECIAL, owner, "<init>", descriptor, false);
    if (popReturnValue) {
      v.visitInsn(Opcodes.POP);
    }
  }

}
