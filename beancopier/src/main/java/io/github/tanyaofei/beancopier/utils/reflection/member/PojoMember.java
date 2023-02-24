package io.github.tanyaofei.beancopier.utils.reflection.member;

import io.github.tanyaofei.beancopier.utils.GenericType;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author tanyaofei
 */
@Accessors
@EqualsAndHashCode
public class PojoMember implements BeanMember {

  private final Field field;
  private final Method method;
  private final GenericType<?> genericType;

  public PojoMember(Field field, Method method) {
    this.field = field;
    this.method = method;
    this.genericType = GenericType.of(field);
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
  public GenericType<?> getType() {
    return this.genericType;
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