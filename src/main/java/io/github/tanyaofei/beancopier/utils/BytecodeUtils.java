package io.github.tanyaofei.beancopier.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

/**
 * @author tanyaofei
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BytecodeUtils implements Opcodes {

  /**
   * 将类名转换为内部名
   *
   * @param className 类名, e.g io.github.tanyaofei.Foo
   * @return 内部名 e.g: io/github/tanyaofei/Foo
   */
  public static String classNameToInternalName(String className) {
    return className.replace(".", "/");
  }

  /**
   * 调用方法
   *
   * @param v 方法编写器
   * @param opcode  操作码
   * @param method  方法
   */
  public static void invokeMethod(MethodVisitor v, int opcode, Method method) {
    v.visitMethodInsn(opcode,
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
   * @param v 方法编写器
   * @param objType 对象类型
   */
  public static void invokeNoArgsConstructor(MethodVisitor v, Class<?> objType) {
    v.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(objType), "<init>", "()V", false);
  }

  public static void newObject(MethodVisitor v, Class<?> objType) {
    v.visitTypeInsn(NEW, Type.getInternalName(objType));
  }

  public static void newInstanceViaNoArgsConstructor(MethodVisitor v, Class<?> objType) {
    newObject(v, objType);
    v.visitInsn(DUP);
    invokeNoArgsConstructor(v, objType);
  }

}
