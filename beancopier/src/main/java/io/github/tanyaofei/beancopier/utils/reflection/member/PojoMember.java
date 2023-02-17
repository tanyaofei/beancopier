package io.github.tanyaofei.beancopier.utils.reflection.member;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author tanyaofei
 */
@Accessors
@EqualsAndHashCode
public class PojoMember implements BeanMember {

  private final Field field;
  private final Method method;

  public PojoMember(Field field, Method method) {
    this.field = field;
    this.method = method;
  }

  @Override
  public Object getIdentify() {
    return field;
  }

  @Override
  public String getName() {
    return field.getName();
  }

  @Override
  public Class<?> getType() {
    return field.getType();
  }

  @Override
  public Type getGenericType() {
    return field.getGenericType();
  }

  @Override
  public Method getMethod() {
    return method;
  }

  @Override
  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    return field.getAnnotation(annotationClass);
  }
}