package io.github.tanyaofei.beancopier.core.local;

import io.github.tanyaofei.beancopier.constants.LocalOpcode;
import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A tool for generating bytecode to define a local variable
 */
public abstract class LocalDefiner implements Opcodes {

  /**
   * Fallback definer. If the definer cannot handle the variable definition,
   * it will be passed to the fallback definer for definition.
   */
  protected LocalDefiner fallback;

  /**
   * Pushing the source object from the local variable to stack
   *
   * @param v method writer
   */
  protected static void loadSource(MethodVisitor v) {
    v.visitVarInsn(Opcodes.ALOAD, 1);
  }

  /**
   * Storing a variable from the stack to the local variable table
   *
   * @param v       method writer
   * @param type    the type of this variable
   * @param context the context of the definition
   */
  protected static void storeLocal(MethodVisitor v, Class<?> type, LocalsDefinitionContext context) {
    var store = context.getNextStore();
    var op = LocalOpcode.ofType(type);
    v.visitVarInsn(op.storeOpcode, store);
    context.setNextStore(store + op.slots);
  }

  /**
   * Storing a variable from the stack to the local variable table
   *
   * @param v          method writer
   * @param definition the definition of local variable
   * @since 0.2.0
   */
  protected static void storeLocal(MethodVisitor v, LocalDefinition definition, LocalsDefinitionContext context) {
    var store = context.getNextStore();
    var op = LocalOpcode.ofType(definition.getType());
    v.visitVarInsn(op.storeOpcode, store);
    context.setNextStore(store + op.slots);
  }

  /**
   * Pushing a variable at specified index from the local variable table to the stack
   *
   * @param v     method writer
   * @param type  the type of variable
   * @param store the index in local variable table
   */
  protected static void loadStack(MethodVisitor v, Class<?> type, int store) {
    v.visitVarInsn(LocalOpcode.ofType(type).loadOpcode, store);
  }

  /**
   * Pushing the reference to <b>{@code this}</b>  from the local variable to the stack
   *
   * @param v method writer
   */
  protected static void loadThis(MethodVisitor v) {
    v.visitVarInsn(Opcodes.ALOAD, 0);
  }

  /**
   * Defining a variable as expected.
   * The implementation should define a variable as expected in this method.
   * If the implementation can not handle, return {@code false} otherwise it should return {@code true}.
   *
   * @param v                   method writer
   * @param converterDefinition the definition of converter
   * @param localDefinition     the definition of the local variable expected
   * @param context             the definition context of all the local variables needed
   * @return true if the local defined as expected, otherwise false.
   * @since 0.2.0
   */
  protected abstract boolean defineInternal(
      MethodVisitor v,
      ConverterDefinition converterDefinition,
      LocalDefinition localDefinition,
      LocalsDefinitionContext context
  );

  /**
   * Set the fallback definer to delegate to when current cannot handle it.
   *
   * @param definer fallback definer
   * @return fallback definer, for chain set
   */
  public LocalDefiner fallbackTo(LocalDefiner definer) {
    this.fallback = definer;
    return definer;
  }

  /**
   * Defining a local variable
   *
   * @param v                   method writer
   * @param converterDefinition the definition of converter
   * @param localDefinition     the definition of the local variable expected
   * @param context             the definition context of all the local variables needed
   * @throws IllegalStateException if none of definers can handle in its chain
   * @since 0.2.0
   */
  public final void define(
      MethodVisitor v,
      ConverterDefinition converterDefinition,
      LocalDefinition localDefinition,
      LocalsDefinitionContext context
  ) {
    var definer = this;
    while (definer != null) {
      if (definer.defineInternal(v, converterDefinition, localDefinition, context)) {
        return;
      }
      definer = definer.fallback;
    }
    throw new IllegalStateException("none of definer can handle: " + localDefinition);
  }

  /**
   * Root definer, do not handle any definitions
   */
  static class RootLocalDefiner extends LocalDefiner {

    @Override
    protected boolean defineInternal(MethodVisitor v, ConverterDefinition converterDefinition, LocalDefinition localDefinition, LocalsDefinitionContext context) {
      return false;
    }
  }

}
