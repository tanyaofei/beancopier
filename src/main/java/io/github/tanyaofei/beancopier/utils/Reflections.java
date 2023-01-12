package io.github.tanyaofei.beancopier.utils;

import io.github.tanyaofei.guava.common.collect.Iterables;
import lombok.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tanyaofei
 * @since 0.0.1
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Reflections {

  /**
   * 通过 internalName 获取 class 的 simpleName
   * <pre>{@code
   *
   *  String simpleName = Reflection.getClassSimpleNameByInternalName(
   *    "io/tanyaofei/beancopier/converter/TestConverter"
   *  );
   *  assert simpleName.equals("TestConverter")
   * }
   * </pre>
   *
   * @param internalName JAVA 内部名
   * @return class 的 simpleName
   */
  public static String getClassSimpleNameByInternalName(String internalName) {
    return internalName.substring(internalName.lastIndexOf("/") + 1);
  }

  /**
   * 获取一个对象包括父类所有的 getter
   * <p>
   * 如果某个字段没有 getter 或者 getter 没有对应的字段, 则不会在返回值
   * </p>
   *
   * @param c              类
   * @param includingSuper 是否包含父类 getters
   * @return 该类包括父类的所有 getter 集合迭代器
   */
  public static Iterable<BeanProperty> getBeanGetters(Class<?> c, boolean includingSuper) {
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
    if (sc != null && sc != Object.class && includingSuper) {
      return Iterables.concat(properties, getBeanGetters(sc, true));
    }

    return properties;
  }

  /**
   * 获取一个对象包括父类所有的 setter
   * <p>
   * 如果某个字段没有 setter 或者 setter 没有对应的字段, 则不会在返回值
   * </p>
   *
   * @param c              类
   * @param includingSuper 是否包含父类 setters
   * @return 该类包括父类的所有 setter 集合迭代器
   */
  public static Iterable<BeanProperty> getBeanSetters(Class<?> c, boolean includingSuper) {
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
    if (sc != null && sc != Object.class && includingSuper) {
      return Iterables.concat(properties, getBeanSetters(sc, true));
    }

    return properties;
  }


  @Getter
  @ToString
  @AllArgsConstructor
  public static class BeanProperty {

    /**
     * 字段名称
     */
    private String name;

    /**
     * 字段反射对象
     */
    private Field field;

    /**
     * getter or setter
     *
     * @see #getBeanGetters(Class, boolean)
     * @see #getBeanSetters(Class, boolean)
     */
    private Method xetter;

    public static Map<String, BeanProperty> mapIterable(Iterable<BeanProperty> itr) {
      Map<String, BeanProperty> map = new HashMap<>();
      for (BeanProperty p : itr) {
        map.put(p.getName(), p);
      }
      return map;
    }


  }

  /**
   * 判断一个类是否是封闭类, 如果一个类是成员类但是不是 static 则不是封闭类
   *
   * @param c 类
   * @return 如果这个类是封闭类则返回 true，反之为 false
   */
  public static boolean isEnclosingClass(Class<?> c) {
    return !c.isMemberClass() || Modifier.isStatic(c.getModifiers());
  }

  public static boolean hasPublicNoArgsConstructor(Class<?> c) {
    try {
      return (Modifier.isPublic(c.getConstructor().getModifiers()));
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  /**
   * 判断 superClassLoader 是否是 subClassLoader 的祖宗
   *
   * @param superClassLoader 可能为祖宗的 classloader
   * @param subClassLoader   可能为子孙的 classloader
   * @return true 表示 superClassLoader 是 subClassLoader, 反则为 false
   */
  public static boolean isCLAssignableFrom(ClassLoader superClassLoader, ClassLoader subClassLoader) {
    if (superClassLoader == subClassLoader) {
      return true;
    }

    // Object.class.getClassLoader() == null
    if (superClassLoader == null) {
      return true;
    }
    if (subClassLoader == null) {
      return false;
    }

    ClassLoader p = subClassLoader;

    while (p.getParent() != null) {
      if (p == superClassLoader) {
        return true;
      }
      p = p.getParent();
    }

    return false;
  }
}
