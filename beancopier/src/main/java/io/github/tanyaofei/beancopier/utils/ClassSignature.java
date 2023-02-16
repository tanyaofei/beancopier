package io.github.tanyaofei.beancopier.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.var;
import org.objectweb.asm.Type;

/**
 * 类签名工具
 *
 * @author tanyaofei
 * @since 0.1.6
 */
public class ClassSignature {

  public static String getClassSignature(ClassInfo superclass, ClassInfo... interfaces) {
    if (superclass == null) {
      throw new IllegalArgumentException("superclass is null");
    }
    if (interfaces == null || interfaces.length == 0) {
      return superclass.getDescriptors();
    }

    var builder = new StringBuilder(superclass.getDescriptors());
    for (var i : interfaces) {
      builder.append(i.getDescriptors());
    }
    return builder.toString();
  }

  /**
   * 类信息
   * <p>
   * 假设一个类是 Map&lt;String, Object&gt;，则对应的 ClassInfo 为  to  type: Map.class, genericTypes: [String.class,
   * Object.class]
   * </p>
   *
   * @author tanyaofei
   * @since 0.0.1
   */
  @Getter
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class ClassInfo {

    /**
     * 类
     */
    private final Class<?> type;

    /**
     * 范型参数类
     */
    private final Class<?>[] typeArguments;

    public static ClassInfo of(Class<?> type, Class<?>... argumentTypes) {
      return new ClassInfo(type, argumentTypes);
    }

    /**
     * @return 描述符
     */
    public final String getDescriptors() {
      if (typeArguments == null || typeArguments.length == 0) {
        return Type.getDescriptor(type);
      }

      var descriptor = Type.getDescriptor(type);
      var builder = new StringBuilder(descriptor.substring(0, descriptor.length() - 1)).append("<");
      for (var g : typeArguments) {
        builder.append(Type.getDescriptor(g));
      }
      builder.append(">;");
      return builder.toString();
    }

  }
}
