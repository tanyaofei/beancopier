package io.github.tanyaofei.beancopier.core.invoker;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;

/**
 * A tool for generating bytecode to call a constructor
 *
 * @author tanyaofei
 * @see Constructor
 * @since 0.2.0
 */
public class ConstructorInvoker implements ExecutableInvoker {

  /**
   * The internal name of the class that defining the constructor
   */
  private final String owner;

  /**
   * The method descriptor of the constructor
   */
  private final String descriptor;

  public ConstructorInvoker(Constructor<?> constructor) {
    this.owner = Type.getInternalName(constructor.getDeclaringClass());
    this.descriptor = Type.getConstructorDescriptor(constructor);
  }

  /**
   * Return an instance from the no-args-constructor of the specified class
   *
   * @param type class
   * @return An instance of ConstructorInvoker
   * @throws IllegalArgumentException if the specified hasn't no-args-constructor
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
   * Write the bytecode
   *
   * @param v          Method writer
   * @param beforeInit Runnable function after between calling NEW and calling &lt;init&gt;
   */
  public void invoke(MethodVisitor v, Runnable beforeInit) {
    invoke(v, false, beforeInit);
  }

  /**
   * Write the bytecode
   *
   * @param v              Method writer
   * @param popReturnValue Whether pop the return value from stack if it has one
   * @param beforeInit     Runnable function after between calling NEW and calling &lt;init&gt;
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
