package io.github.tanyaofei.beancopier.core.invoker;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A tool for generating bytecode to call a method
 *
 * @author tanyaofei
 * @see Method
 * @since 0.2.0
 */
public class MethodInvoker implements ExecutableInvoker {

  /**
   * The name of method
   */
  private final String name;

  /**
   * Thw owner class name of method
   */
  private final String owner;

  /**
   * The method descriptor
   */
  private final String descriptor;

  /**
   * The opcode to invoke this method
   * {@link Opcodes#INVOKESPECIAL}
   * {@link Opcodes#INVOKEVIRTUAL}
   * {@link Opcodes#INVOKESTATIC}
   * {@link Opcodes#INVOKEINTERFACE}
   */
  private final int opcode;

  /**
   * Whether the declaring class is an interface
   */
  private final boolean isInterface;

  /**
   * Whether the method have return value
   */
  private final boolean hasReturnValue;

  public MethodInvoker(@Nonnull Method method) {
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
    this.hasReturnValue = !method.getReturnType().equals(void.class);
  }

  @Override
  public void invoke(@Nonnull MethodVisitor v, boolean popReturnValue) {
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
