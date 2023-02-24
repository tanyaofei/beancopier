package io.github.tanyaofei.beancopier.utils.reflection.member;

import io.github.tanyaofei.beancopier.utils.GenericType;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tanyaofei
 */
public interface BeanMember {

  @Nonnull
  static Map<String, BeanMember> mapIterable(@Nonnull Iterable<BeanMember> itr) {
    Map<String, BeanMember> map;
    if (itr instanceof Collection<BeanMember> c) {
      map = new HashMap<>(c.size());
    } else {
      map = new HashMap<>();
    }
    itr.forEach(item -> map.put(item.getName(), item));
    return map;
  }

  /**
   * @return identify
   */
  @Nonnull
  Object getIdentify();

  /**
   * @return member name
   */
  @Nonnull
  String getName();

  /**
   * @return member generic type
   */
  @Nonnull
  GenericType<?> getType();

  /**
   * @return getter or setter
   * @throws UnsupportedOperationException throw if the member has none
   */
  @Nullable
  Method getMethod();

  /**
   * Return an annotation from the origin
   *
   * @param annotationClass annotation class
   * @param <T>             the type of annotation
   * @return 注解
   */
  @Nullable <T extends Annotation> T getAnnotation(@Nonnull Class<T> annotationClass);

}
