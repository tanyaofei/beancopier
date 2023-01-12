package io.github.tanyaofei.beancopier;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 字节码方法调用工具
 *
 * @author tanyaofei
 * @since 0.1.6
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class MethodInvoker {

  /**
   * method name
   */
  private final String name;

  /**
   * method owner (internalName)
   */
  private final String owner;

  /**
   * method descriptor
   */
  private final String descriptor;

  /**
   * invoke opcode
   */
  private final int opcode;

  /**
   * declaring class is an interface or not
   */
  private final boolean isInterface;

  /**
   * method has return value or not
   */
  private final boolean hasReturnValue;

  /**
   * method is constructor or not
   */
  private final boolean isConstructor;

  public static MethodInvoker constructorInvoker(Class<?> c) {
    Constructor<?> constructor;
    try {
      constructor = c.getConstructor();
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(c + " has not a no args constructor", e);
    }
    return new MethodInvoker(
        MethodNames.Object$init,
        Type.getInternalName(c),
        Type.getConstructorDescriptor(constructor),
        Opcodes.INVOKESPECIAL,
        false,
        false,
        true
    );
  }

  public static MethodInvoker methodInvoker(Method method) {
    boolean isInterface = method.getDeclaringClass().isInterface();
    int opcode;
    if (Modifier.isStatic(method.getModifiers())) {
      opcode = Opcodes.INVOKESTATIC;
    } else if (isInterface) {
      opcode = Opcodes.INVOKEINTERFACE;
    } else {
      opcode = Opcodes.INVOKEVIRTUAL;
    }

    return new MethodInvoker(
        method.getName(),
        Type.getInternalName(method.getDeclaringClass()),
        Type.getMethodDescriptor(method),
        opcode,
        isInterface,
        !method.getReturnType().equals(Void.class),
        false
    );
  }

  private void invokeConstructor(MethodVisitor v, boolean popReturnValue) {
    v.visitTypeInsn(Opcodes.NEW, owner);
    v.visitInsn(Opcodes.DUP);
    invoke0(v, popReturnValue);
  }

  private void invoke0(MethodVisitor v, boolean popReturnValue) {
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

  public void invoke(MethodVisitor v) {
    invoke(v, false);
  }

  public void invoke(MethodVisitor v, boolean popReturnValue) {
    if (isConstructor) {
      invokeConstructor(v, popReturnValue);
    } else {
      invoke0(v, popReturnValue);
    }
  }

  @Override
  public String toString() {
    return this.owner + "." + name;
  }

}
