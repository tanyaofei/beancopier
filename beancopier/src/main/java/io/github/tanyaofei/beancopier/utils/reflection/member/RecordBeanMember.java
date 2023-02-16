package io.github.tanyaofei.beancopier.utils.reflection.member;

import lombok.EqualsAndHashCode;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;

/**
 * @author tanyaofei
 */
@EqualsAndHashCode
public class RecordBeanMember implements BeanMember {

  private final RecordComponent rc;
  private final Method xetter;

  public RecordBeanMember(RecordComponent rc, Method xetter) {
    this.rc = rc;
    this.xetter = xetter;
  }

  @Override
  public String getName() {
    return rc.getName();
  }

  @Override
  public Class<?> getType() {
    return rc.getType();
  }

  @Override
  public Type getGenericType() {
    return rc.getGenericType();
  }

  @Override
  public Method getMethod() {
    return xetter;
  }

  @Override
  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    return rc.getAnnotation(annotationClass);
  }
}