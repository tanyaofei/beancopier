package io.github.tanyaofei.beancopier.utils;

import com.google.common.collect.Iterables;
import lombok.*;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
   * 获取一个对象包括父类所有的 getter
   * <p>
   * 如果某个字段没有 getter 或者 getter 没有对应的字段, 则不会在返回值丽
   * </p>
   *
   * @param c 类
   * @return 该类包括父类的所有 getter 集合迭代器
   */
  public static Iterable<BeanProperty> getBeanGetters(Class<?> c) {
    Field[] fields = c.getDeclaredFields();
    ArrayList<BeanProperty> properties = new ArrayList<>(fields.length);
    for (Field f : fields) {
      String name = f.getName();
      Method getter;
      try {
        if (f.getType() == boolean.class) {
          getter = c.getDeclaredMethod("is" + StringUtils.capitalize(name));
        } else {
          getter = c.getDeclaredMethod("get" + StringUtils.capitalize(name));
        }
      } catch (NoSuchMethodException e) {
        continue;
      }
      if (getter.getParameterTypes().length != 0) {
        continue;
      }

      properties.add(new BeanProperty(name, f, getter));
    }

    Class<?> sc = c.getSuperclass();
    if (sc != null && sc != Object.class) {
      return Iterables.concat(properties, getBeanGetters(sc));
    }

    return properties;
  }

  /**
   * 获取一个对象包括父类所有的 setter
   * <p>
   * 如果某个字段没有 setter 或者 setter 没有对应的字段, 则不会在返回值丽
   * </p>
   *
   * @param c 类
   * @return 该类包括父类的所有 setter 集合迭代器
   */
  public static Iterable<BeanProperty> getBeanSetters(Class<?> c) {
    Field[] fields = c.getDeclaredFields();
    ArrayList<BeanProperty> properties = new ArrayList<>(fields.length);
    for (Field f : fields) {
      String name = f.getName();
      Method setter;
      try {
        setter = c.getDeclaredMethod("set" + StringUtils.capitalize(f.getName()), f.getType());
      } catch (NoSuchMethodException e) {
        continue;
      }
      if (setter.getParameterTypes().length != 1) {
        continue;
      }

      properties.add(new BeanProperty(name, f, setter));
    }

    Class<?> sc = c.getSuperclass();
    if (sc != null && sc != Object.class) {
      return Iterables.concat(properties, getBeanSetters(sc));
    }

    return properties;
  }


  @Getter
  @ToString
  @AllArgsConstructor
  public static class BeanProperty {
    private String name;
    private Field field;
    private Method xtter;


    public static Map<String, BeanProperty> mapIterable(Iterable<BeanProperty> itr) {
      Map<String, BeanProperty> map = new HashMap<>();
      for (BeanProperty p : itr) {
        map.put(p.getName(), p);
      }
      return map;
    }


  }


}
