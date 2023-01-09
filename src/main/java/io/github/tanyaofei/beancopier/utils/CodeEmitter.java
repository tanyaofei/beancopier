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
public class CodeEmitter implements Opcodes {


  /**
   * 批量获取类描述符
   *
   * @param classes 类数组
   * @return 类描述符
   */
  public static String getClassDescriptors(Class<?>... classes) {
    StringBuilder builder = new StringBuilder();
    for (Class<?> clazz : classes) {
      builder.append(Type.getDescriptor(clazz));
    }
    return builder.toString();
  }

  /**
   * 获取方法描述符
   *
   * @param rType  方法返回值类
   * @param pTypes 方法参数类型数组
   * @return 方法描述符
   */
  public static String getMethodDescriptor(Class<?> rType, Class<?>... pTypes) {
    StringBuilder builder = new StringBuilder(pTypes.length + 4);
    builder.append("(");
    for (Class<?> type : pTypes) {
      builder.append(Type.getDescriptor(type));
    }
    return builder.append(")").append(Type.getDescriptor(rType)).toString();
  }

  /**
   * 获取类的签名
   *
   * @param superClass 父类
   * @param interfaces 接口
   * @return 类描述符
   */
  public static String getClassSignature(ClassInfo superClass, ClassInfo... interfaces) {
    if (superClass == null) {
      throw new IllegalArgumentException("superClass is null");
    }
    if (interfaces == null || interfaces.length == 0) {
      return superClass.getDescriptors();
    }

    StringBuilder builder = new StringBuilder(superClass.getDescriptors());
    for (ClassInfo i : interfaces) {
      builder.append(i.getDescriptors());
    }
    return builder.toString();
  }


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
