package io.github.tanyaofei.beancopier.utils.reflection.member;

import io.github.tanyaofei.beancopier.utils.GenericType;
import lombok.EqualsAndHashCode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

/**
 * @author tanyaofei
 */
@EqualsAndHashCode
public class RecordMember implements BeanMember {

  @Nonnull
  private final RecordComponent rc;

  @Nullable
  private final Method method;

  @Nonnull
  private final GenericType<?> genericType;

  public RecordMember(@Nonnull RecordComponent rc, @Nullable Method method) {
    this.rc = rc;
    this.method = method;
    this.genericType = GenericType.of(rc);
  }

  @Nonnull
  @Override
  public Object getIdentify() {
    return rc;
  }

  @Nonnull
  @Override
  public String getName() {
    return rc.getName();
  }

  @Nonnull
  @Override
  public GenericType<?> getType() {
    return this.genericType;
  }

  @Override
  @Nullable
  public Method getMethod() {
    return method;
  }

  @Nullable
  @Override
  public <T extends Annotation> T getAnnotation(@Nonnull Class<T> annotationClass) {
    return rc.getAnnotation(annotationClass);
  }
}