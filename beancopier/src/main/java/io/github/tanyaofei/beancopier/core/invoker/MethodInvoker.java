package io.github.tanyaofei.beancopier.core.invoker;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 方法调用字节码编写工具
 *
 * @author tanyaofei
 * @since 0.2.0
 */
public class MethodInvoker implements ExecutableInvoker {

  /**
   * 方法名称
   */
  private final String name;

  /**
   * 定义该方法的类的内部名
   */
  private final String owner;

  /**
   * 方法描述符
   */
  private final String descriptor;

  /**
   * 方法调用字节码指令
   */
  private final int opcode;

  /**
   * 定义该方法的类是否时一个接口
   */
  private final boolean isInterface;

  /**
   * 该方法是否具有返回值
   */
  private final boolean hasReturnValue;

  /**
   * 创建一个方法调用字节码编写工具
   *
   * @param method 方法
   */
  public MethodInvoker(Method method) {
    this.name = method.getName();
    this.owner = Type.getInternalName(method.getDeclaringClass());
    this.descriptor = Type.getMethodDescriptor(method);
    this.isInterface = method.getDeclaringClass().isInterface();
    if (Modifier.isStatic(method.getModifiers())) {
      this.opcode = Opcodes.INVOKESTATIC;
    } else if (isInterface) {
      this.opcode = Opcodes.INVOKEINTERFACE;
    } else {
      this.opcode = Opcodes.INVOKEVIRTUAL;
    }
    this.hasReturnValue = !method.getReturnType().equals(Void.class);
  }

  @Override
  public void invoke(MethodVisitor v, boolean popReturnValue) {
    v.visitMethodInsn(
        opcode,
        owner,
        name,
        descriptor,
        isInterface
    );

    if (popReturnValue && hasReturnValue) {
      v.visitInsn(Opcodes.POP);
    }
  }
}
