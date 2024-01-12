package io.github.tanyaofei.beancopier.core.local;

import io.github.tanyaofei.beancopier.constants.TypedOpcode;
import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
  @Nullable
  protected LocalDefiner fallback;

  /**
   * Pushing the source object from the local variable to stack
   *
   * @param v method writer
   */
  protected static void loadSource(@NotNull MethodVisitor v) {
    v.visitVarInsn(Opcodes.ALOAD, 1);
  }

  /**
   * Storing a variable from the stack to the local variable table
   *
   * @param v       method writer
   * @param type    the type of this variable
   * @param context the context of the definition
   */
  protected static void storeLocal(
      @NotNull MethodVisitor v,
      @NotNull Class<?> type,
      @NotNull LocalsDefinitionContext context
  ) {
    var store = context.getNextStore();
    var op = TypedOpcode.ofType(type);
    v.visitVarInsn(op.store, store);
    context.setNextStore(store + op.slots);
  }

  /**
   * Storing a variable from the stack to the local variable table
   *
   * @param v          method writer
   * @param definition the definition of local variable
   * @since 0.2.0
   */
  protected static void storeLocal(
      @NotNull MethodVisitor v,
      @NotNull LocalDefinition definition,
      @NotNull LocalsDefinitionContext context
  ) {
    var store = context.getNextStore();
    var op = TypedOpcode.ofType(definition.getType().getRawType());
    v.visitVarInsn(op.store, store);
    context.setNextStore(store + op.slots);
  }

  /**
   * Pushing a variable at specified index from the local variable table to the stack
   *
   * @param v     method writer
   * @param type  the type of variable
   * @param store the index in local variable table
   */
  protected static void loadStack(@NotNull MethodVisitor v, @NotNull Class<?> type, int store) {
    v.visitVarInsn(TypedOpcode.ofType(type).load, store);
  }

  /**
   * Pushing the reference to <b>{@code this}</b>  from the local variable to the stack
   *
   * @param v method writer
   */
  protected static void loadThis(@NotNull MethodVisitor v) {
    v.visitVarInsn(Opcodes.ALOAD, 0);
  }

  /**
   * Defining a variable as expected.
   * The implementation should define a variable as expected in this method.
   * If the implementation can not handle, return {@code false} otherwise it should return {@code true}.
   *
   * @param v         method writer
   * @param converter the definition of converter
   * @param local     the definition of the local variable expected
   * @param context   the definition context of all the local variables needed
   * @return true if the local defined as expected, otherwise false.
   * @since 0.2.0
   */
  protected abstract boolean defineInternal(
      @NotNull MethodVisitor v,
      @NotNull ConverterDefinition converter,
      @NotNull LocalDefinition local,
      @NotNull LocalsDefinitionContext context
  );

  /**
   * Set the fallback definer to delegate to when current cannot handle it.
   *
   * @param definer fallback definer
   * @return fallback definer, for chain set
   */
  @NotNull
  public LocalDefiner fallbackTo(@NotNull LocalDefiner definer) {
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
      @NotNull MethodVisitor v,
      @NotNull ConverterDefinition converterDefinition,
      @NotNull LocalDefinition localDefinition,
      @NotNull LocalsDefinitionContext context
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
    protected boolean defineInternal(
        @NotNull MethodVisitor v,
        @NotNull ConverterDefinition converter,
        @NotNull LocalDefinition local,
        @NotNull LocalsDefinitionContext context
    ) {
      return false;
    }
  }

}
