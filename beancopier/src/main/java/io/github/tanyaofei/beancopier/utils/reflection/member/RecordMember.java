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
public class RecordMember implements BeanMember {

  private final RecordComponent rc;
  private final Method method;

  public RecordMember(RecordComponent rc, Method method) {
    this.rc = rc;
    this.method = method;
  }

  @Override
  public Object getIdentify() {
    return rc;
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
    return method;
  }

  @Override
  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    return rc.getAnnotation(annotationClass);
  }
}