package io.github.tanyaofei.beancopier.utils.reflection.member;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tanyaofei
 */
public interface BeanMember {

  static Map<String, BeanMember> mapIterable(Iterable<BeanMember> itr) {
    Map<String, BeanMember> map = new HashMap<>();
    for (var m : itr) {
      map.put(m.getName(), m);
    }
    return map;
  }

  /**
   * @return 属性名称
   */
  String getName();

  /**
   * @return 字段类型
   */
  Class<?> getType();

  /**
   * @return 字段泛型
   */
  Type getGenericType();

  /**
   * @return getter or setter
   * @throws UnsupportedOperationException 如果该属性没有定义 getter 或者 setter
   */
  Method getMethod();

  /**
   * 获取属性的注解
   *
   * @param annotationClass 注解类型
   * @param <T>             注解类型
   * @return 注解
   */
  @Nullable <T extends Annotation> T getAnnotation(Class<T> annotationClass);

}
