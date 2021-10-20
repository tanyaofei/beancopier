package com.github.tanyaofei.beancopier.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

/**
 * @author tanyaofei
 * @since 2021.08.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BytecodeUtils implements Opcodes {

  /**
   * 调用方法
   *
   * @param visitor 方法编写器
   * @param opcode  操作码
   * @param method  方法
   */
  public static void invokeMethod(MethodVisitor visitor, int opcode, Method method) {
    visitor.visitMethodInsn(opcode,
        Type.getInternalName(method.getDeclaringClass()),
        method.getName(),
        Type.getMethodDescriptor(method),
        method.getDeclaringClass().isInterface());
  }

  /**
   * 调用对象初始化方法
   * <pre>
   *   INVOKESPECIAL #objType &lt;init&gt;()V
   * </pre>
   *
   * @param visitor 方法编写器
   * @param objType 对象类型
   */
  public static void invokeNoArgsConstructor(MethodVisitor visitor, Class<?> objType) {
    visitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(objType), "<init>", "()V", false);
  }

}
