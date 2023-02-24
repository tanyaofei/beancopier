package io.github.tanyaofei.beancopier.utils.reflection;

import io.github.tanyaofei.beancopier.utils.StringUtils;
import io.github.tanyaofei.beancopier.utils.reflection.member.BeanMember;
import io.github.tanyaofei.beancopier.utils.reflection.member.PojoMember;
import io.github.tanyaofei.beancopier.utils.reflection.member.RecordMember;
import io.github.tanyaofei.beancopier.utils.reflection.member.SettableRecordMember;
import io.github.tanyaofei.guava.common.collect.Iterables;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;
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
   * Return the simple name by the specified internal name of class
   * <pre>{@code
   *  var simpleName = Reflections.getClassSimpleNameByInternalName("java/base/Object");
   *  assertEquals("Object", simpleName);
   * }</pre>
   *
   * @param internalName internal name
   * @return simple class name
   */
  @Nonnull
  public static String getClassSimpleNameByInternalName(@Nonnull String internalName) {
    return internalName.substring(internalName.lastIndexOf("/") + 1);
  }

  @Nonnull
  public static String getInternalNameByClassName(@Nonnull String className) {
    return className.replace(".", "/");
  }

  /**
   * Return bean members and the {@link BeanMember#getMethod()} is a getter
   *
   * @param c              class
   * @param includingSuper whether including super members or not
   * @return bean members
   */
  @Nonnull
  public static Iterable<BeanMember> getGettableBeanMember(@Nonnull Class<?> c, boolean includingSuper) {
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

      properties.add(new PojoMember(field, getter));
    }

    var superclass = c.getSuperclass();
    if (superclass != null && superclass != Object.class && includingSuper) {
      return Iterables.concat(properties, getGettableBeanMember(superclass, true));
    }

    return properties;
  }

  /**
   * Return bean members and the {@link BeanMember#getMethod()} is a getter
   *
   * @param c record class
   * @return bean members
   */
  @Nonnull
  private static Iterable<BeanMember> getRecordMembersWithGetter(@Nonnull Class<?> c) {
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
   * Return bean members and {@link BeanMember#getMethod()} is setter
   *
   * @param c              specified class
   * @param includingSuper whether including super members or not
   * @return bean members
   */
  @Nonnull
  public static Iterable<BeanMember> getMembersWithSetter(@Nonnull Class<?> c, boolean includingSuper) {
    if (c.isRecord()) {
      return getSettableRecordMember(c);
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

      properties.add(new PojoMember(f, setter));
    }

    Class<?> superclass = c.getSuperclass();
    if (superclass != null && superclass != Object.class && includingSuper) {
      return Iterables.concat(properties, getMembersWithSetter(superclass, true));
    }

    return properties;
  }

  /**
   * Return record members and each of them has no setter
   *
   * @param c the specified class
   * @return bean members
   */
  @Nonnull
  private static Iterable<BeanMember> getSettableRecordMember(@Nonnull Class<?> c) {
    if (!c.isRecord()) {
      throw new IllegalArgumentException(c.getName() + " is not a record class");
    }
    return Arrays
        .stream(c.getRecordComponents())
        .map(SettableRecordMember::new)
        .collect(Collectors.toList());
  }

  /**
   * Return whether the specified class is an enclosing class
   *
   * @param c the specified class
   * @return true if the specified class is an enclosing otherwise false
   */
  public static boolean isEnclosingClass(@Nonnull Class<?> c) {
    return !c.isMemberClass() || Modifier.isStatic(c.getModifiers());
  }

  /**
   * Return whether the specified has a public no-args-constructor
   *
   * @param c the specified class
   * @return true if the specified class has a public no-args-constructor otherwise false
   */
  public static boolean hasPublicNoArgsConstructor(@Nonnull Class<?> c) {
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
   * Return the specified has an all-args-constructor
   *
   * @param c the specified
   * @return true if the specified class has an all-args-constructor otherwise false
   */
  public static boolean hasPublicAllArgsConstructor(@Nonnull Class<?> c) {
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
