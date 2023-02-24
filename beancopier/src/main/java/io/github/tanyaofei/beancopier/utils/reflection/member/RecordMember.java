package io.github.tanyaofei.beancopier.utils.reflection.member;

import io.github.tanyaofei.beancopier.utils.GenericType;
import lombok.EqualsAndHashCode;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

/**
 * @author tanyaofei
 */
@EqualsAndHashCode
public class RecordMember implements BeanMember {

  private final RecordComponent rc;
  private final Method method;
  private final GenericType<?> genericType;

  public RecordMember(RecordComponent rc, Method method) {
    this.rc = rc;
    this.method = method;
    this.genericType = GenericType.of(rc);
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
  public GenericType<?> getType() {
    return this.genericType;
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