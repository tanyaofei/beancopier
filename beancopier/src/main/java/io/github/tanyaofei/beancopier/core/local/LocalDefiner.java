package io.github.tanyaofei.beancopier.core.local;

import io.github.tanyaofei.beancopier.constants.LocalOpcode;
import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 局部变量定义器
 */
public abstract class LocalDefiner implements Opcodes {

  /**
   * 下游委托定义器
   */
  protected LocalDefiner fallback;

  /**
   * 将局部变量表中的 source 对象加载到栈帧中
   *
   * @param v 方法编写器
   * @since 0.2.0
   */
  protected static void loadSource(MethodVisitor v) {
    v.visitVarInsn(Opcodes.ALOAD, 1);
  }

  /**
   * 将栈帧中第一个值到局部变量表
   *
   * @param v       方法编写器
   * @param type    值类型
   * @param context 变量定义上下文
   * @since 0.2.0
   */
  protected static void storeLocal(MethodVisitor v, Class<?> type, LocalsDefinitionContext context) {
    var store = context.getNextStore();
    var op = LocalOpcode.ofType(type);
    v.visitVarInsn(op.storeOpcode, store);
    context.setNextStore(store + op.slots);
  }

  /**
   * 将栈帧中第一个值到局部变量表
   *
   * @param v          方法编写器
   * @param definition 局部变量定义
   * @since 0.2.0
   */
  protected static void storeLocal(MethodVisitor v, LocalDefinition definition, LocalsDefinitionContext context) {
    var store = context.getNextStore();
    var op = LocalOpcode.ofType(definition.getType());
    v.visitVarInsn(op.storeOpcode, store);
    context.setNextStore(store + op.slots);
  }

  /**
   * 将局部变量表中指定下标的值加载到栈帧
   *
   * @param v     方法编写器
   * @param type  值类型
   * @param store 该值位于局部变量表中的下标
   * @since 0.2.0
   */
  protected static void loadStack(MethodVisitor v, Class type, int store) {
    v.visitVarInsn(LocalOpcode.ofType(type).loadOpcode, store);
  }

  /**
   * 将局部变量表中的 this 对象加载到栈帧
   *
   * @param v 方法编写器
   * @since 0.2.0
   */
  protected static void loadThis(MethodVisitor v) {
    v.visitVarInsn(Opcodes.ALOAD, 0);
  }

  /**
   * 定义局部变量
   *
   * @param v                   方法编写器
   * @param converterDefinition 转换器定义
   * @param localDefinition     局部变量定义
   * @param context             定义上下文
   * @return 是否成功定义, true 表示该变量的定义已经结束, false 表示当前实现类无法对该变量定义并交给 {@link #fallback} 去定义
   * @since 0.2.0
   */
  protected abstract boolean defineInternal(
      MethodVisitor v,
      ConverterDefinition converterDefinition,
      LocalDefinition localDefinition,
      LocalsDefinitionContext context
  );

  /**
   * 定义当前定义器无法定义式委托的定义器
   *
   * @param definer 委托定义器
   * @return 委托定义器
   * @since 0.2.0
   */
  public LocalDefiner fallbackTo(LocalDefiner definer) {
    this.fallback = definer;
    return definer;
  }

  /**
   * 定义局部变量
   *
   * @param v                   方法编写器
   * @param converterDefinition 转换器定义
   * @param localDefinition     局部变量定义
   * @param context             定义上下文
   * @throws IllegalStateException 如果所有委托定义器都无法将其定义
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
   * 根定义器, 不进行任何操作, 将委托给下游定义器
   *
   * @since 0.2.0
   */
  static class RootLocalDefiner extends LocalDefiner {

    @Override
    protected boolean defineInternal(MethodVisitor v, ConverterDefinition converterDefinition, LocalDefinition localDefinition, LocalsDefinitionContext context) {
      return false;
    }
  }

}
