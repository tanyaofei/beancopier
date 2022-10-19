package io.github.tanyaofei.beancopier.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tanyaofei
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectUtils {

  /**
   * 批量获取类描述符
   *
   * @param classes 类数组
   * @return 类描述符
   */
  public static String getDescriptors(Class<?>... classes) {
    StringBuilder builder = new StringBuilder();
    for (Class<?> clazz : classes) {
      builder.append(Type.getDescriptor(clazz));
    }
    return builder.toString();
  }

  /**
   * 获取类的签名
   *
   * @param superClass 父类
   * @param interfaces 接口
   * @return 类描述符
   */
  public static String getClassSignature(ClassInfo superClass, ClassInfo... interfaces) {
    assert superClass != null : "SuperClass can not be null";
    if (interfaces == null || interfaces.length == 0) {
      return superClass.getDescriptors();
    }

    var builder = new StringBuilder(superClass.getDescriptors());
    for (var i : interfaces) {
      builder.append(i.getDescriptors());
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
   * 获取一个类的所有字段的 Getter 方法
   *
   * @param target 目标类
   * @return 该类的所有字段的 Getter 方法
   */
  public static Map<String, Tuple<Field, Method>> getFieldGetters(Class<?> target) {
    Field[] fields = target.getDeclaredFields();
    Map<String, Tuple<Field, Method>> getters = new HashMap<>(fields.length, 1.0F);
    for (Field field : fields) {
      Method getter;
      try {
        getter = target.getDeclaredMethod("get" + StringUtils.capitalize(field.getName()));
      } catch (NoSuchMethodException e) {
        continue;
      }
      getters.put(field.getName(), Tuple.of(field, getter));
    }
    return getters;
  }

  /**
   * 获取一个类的所有字段的 Setter 方法, key: 字段名, value: setter 方法
   *
   * @param target 目标类
   * @return 该类的所有字段的 Setter 方法
   */
  public static Map<String, Tuple<Field, Method>> getFieldSetters(Class<?> target) {
    Field[] fields = target.getDeclaredFields();
    Map<String, Tuple<Field, Method>> setters = new HashMap<>(fields.length, 1.0F);
    for (Field field : fields) {
      Method setter;
      try {
        setter = target
            .getDeclaredMethod("set" + StringUtils.capitalize(field.getName()), field.getType());
      } catch (NoSuchMethodException e) {
        continue;
      }
      setters.put(field.getName(), Tuple.of(field, setter));
    }
    return setters;
  }


}
