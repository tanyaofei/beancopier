package io.github.tanyaofei.beancopier.utils.reflection;

import io.github.tanyaofei.beancopier.utils.StringUtils;
import io.github.tanyaofei.beancopier.utils.reflection.member.BeanMember;
import io.github.tanyaofei.beancopier.utils.reflection.member.POJOMember;
import io.github.tanyaofei.beancopier.utils.reflection.member.RecordMember;
import io.github.tanyaofei.beancopier.utils.reflection.member.SettableRecordMember;
import io.github.tanyaofei.guava.common.collect.Iterables;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author tanyaofei
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

  public static String getInternalNameByClassName(String className) {
    return className.replace(".", "/");
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
  public static Iterable<BeanMember> getMembersWithGetter(Class<?> c, boolean includingSuper) {
    if (c.isRecord()) {
      return getRecordMembersWithGetter(c);
    }

    var fields = c.getDeclaredFields();
    var properties = new ArrayList<BeanMember>(fields.length);
    for (var field : fields) {
      var name = field.getName();
      Method getter;
      try {
        if (field.getType() == boolean.class) {
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

      properties.add(new POJOMember(field, getter));
    }

    var superclass = c.getSuperclass();
    if (superclass != null && superclass != Object.class && includingSuper) {
      return Iterables.concat(properties, getMembersWithGetter(superclass, true));
    }

    return properties;
  }

  /**
   * 获取 record 类的 getter
   *
   * @param c record 类
   * @return record 类的 getter 可迭代对象
   * @since 2.0.0
   */
  private static Iterable<BeanMember> getRecordMembersWithGetter(Class<?> c) {
    if (!c.isRecord()) {
      throw new IllegalArgumentException(c.getName() + " is not a record class");
    }

    var properties = new ArrayList<BeanMember>(c.getRecordComponents().length);
    for (var rc : c.getRecordComponents()) {
      try {
        properties.add(new RecordMember(rc, c.getMethod(rc.getName())));
      } catch (NoSuchMethodException e) {
        throw new IllegalStateException(c + " missing a getter for property: " + rc.getName());
      }
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
  public static Iterable<BeanMember> getMembersWithSetter(Class<?> c, boolean includingSuper) {
    if (c.isRecord()) {
      return getRecordMembersWithSetter(c);
    }

    var fields = c.getDeclaredFields();
    var properties = new ArrayList<BeanMember>(fields.length);
    for (var f : fields) {
      Method setter;
      try {
        setter = c.getDeclaredMethod("set" + StringUtils.capitalize(f.getName()), f.getType());
      } catch (NoSuchMethodException e) {
        continue;
      }
      if (setter.getParameterTypes().length != 1) {
        continue;
      }

      properties.add(new POJOMember(f, setter));
    }

    Class<?> superclass = c.getSuperclass();
    if (superclass != null && superclass != Object.class && includingSuper) {
      return Iterables.concat(properties, getMembersWithSetter(superclass, true));
    }

    return properties;
  }

  private static Iterable<BeanMember> getRecordMembersWithSetter(Class<?> c) {
    if (!c.isRecord()) {
      throw new IllegalArgumentException(c.getName() + " is not a record class");
    }
    return Arrays
        .stream(c.getRecordComponents())
        .map(SettableRecordMember::new)
        .collect(Collectors.toList());
  }

  /**
   * 判断 superClassLoader 是否是 subClassLoader 的祖宗
   *
   * @param superClassLoader 可能为祖宗的 classloader
   * @param subClassLoader   可能为子孙的 classloader
   * @return true 表示 superClassLoader 是 subClassLoader, 反则为 false
   */
  public static boolean isClAssignableFrom(ClassLoader superClassLoader, ClassLoader subClassLoader) {
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

    var p = subClassLoader;

    while (p.getParent() != null) {
      if (p == superClassLoader) {
        return true;
      }
      p = p.getParent();
    }

    return false;
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

  /**
   * 判断一个类是否有一个 public 的无参构造函数
   * @param c 类
   * @return 是否有一个 public 的无参构造函数
   */
  public static boolean hasPublicNoArgsConstructor(Class<?> c) {
    if (c.isRecord()) {
      return false;
    }
    try {
      c.getConstructor();
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }


  /**
   * 判断一个类是否包含一个与其所有字段相匹配（同名同顺序）的 public 构造器
   *
   * @param c 类
   * @return 是否包含一个与其所有字段相匹配（同名同顺序）的构造器
   */
  public static boolean hasMatchedPublicAllArgsConstructor(Class<?> c) {
    if (c.isRecord()) {
      return true;
    }
    if (c.getSuperclass() != Object.class && c.getSuperclass() != null) {
      return false;
    }
    var fields = c.getDeclaredFields();

    Constructor<?> constructor;
    try {
      constructor = c.getConstructor(Arrays.stream(fields).map(Field::getType).toArray(Class[]::new));
    } catch (NoSuchMethodException e) {
      return false;
    }

    var parameterNames = LocalVariableTableParameterNameDiscoverer.getParameterNames(constructor);
    if (parameterNames == null) {
      return false;
    }
    for (int i = 0; i < fields.length; i++) {
      if (!fields[i].getName().equals(parameterNames[i])) {
        return false;
      }
    }
    return true;
  }


}
