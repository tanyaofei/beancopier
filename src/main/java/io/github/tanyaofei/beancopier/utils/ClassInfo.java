package io.github.tanyaofei.beancopier.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.objectweb.asm.Type;

/**
 * 类信息
 * <p>
 * 假设一个类是 Map&lt;String, Object&gt;，则对应的 ClassInfo 为  to  type: Map.class, genericTypes: [String.class,
 * Object.class]
 * </p>
 *
 * @author tanyaofei
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassInfo {

  /**
   * 类
   */
  private final Class<?> type;

  /**
   * 范型类
   */
  private final Class<?>[] genericTypes;

  public static ClassInfo of(Class<?> type, Class<?>... genericTypes) {
    return new ClassInfo(type, genericTypes);
  }

  /**
   * @return 描述符
   */
  public final String getDescriptors() {
    if (genericTypes == null || genericTypes.length == 0) {
      return Type.getDescriptor(type);
    }

    String descriptor = Type.getDescriptor(type);
    StringBuilder builder = new StringBuilder(descriptor.substring(0, descriptor.length() - 1)).append("<");
    for (Class<?> g : genericTypes) {
      builder.append(Type.getDescriptor(g));
    }
    builder.append(">;");
    return builder.toString();
  }

}
