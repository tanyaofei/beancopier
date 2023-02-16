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
   * @return identify
   */
  Object getIdentify();

  /**
   * @return member name
   */
  String getName();

  /**
   * @return member type
   */
  Class<?> getType();

  /**
   * @return member generic type
   */
  Type getGenericType();

  /**
   * @return getter or setter
   * @throws UnsupportedOperationException throw if the member has none
   */
  Method getMethod();

  /**
   * Return an annotation from the origin
   *
   * @param annotationClass annotation class
   * @param <T>             the type of annotation
   * @return 注解
   */
  @Nullable <T extends Annotation> T getAnnotation(Class<T> annotationClass);

}
