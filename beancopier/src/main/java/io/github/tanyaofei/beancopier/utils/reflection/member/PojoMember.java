package io.github.tanyaofei.beancopier.utils.reflection.member;

import io.github.tanyaofei.beancopier.utils.GenericType;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author tanyaofei
 */
@Accessors
@EqualsAndHashCode
public class PojoMember implements BeanMember {

  @Nonnull
  private final Field field;

  @Nonnull
  private final Method method;

  @Nonnull
  private final GenericType<?> genericType;

  public PojoMember(@Nonnull Field field, @Nonnull Method method) {
    this.field = field;
    this.method = method;
    this.genericType = GenericType.of(field);
  }

  @Override
  @Nonnull
  public Object getIdentify() {
    return field;
  }

  @Override
  @Nonnull
  public String getName() {
    return field.getName();
  }

  @Override
  @Nonnull
  public GenericType<?> getType() {
    return this.genericType;
  }

  @Override
  @Nonnull
  public Method getMethod() {
    return method;
  }

  @Override
  @Nullable
  public <T extends Annotation> T getAnnotation(@Nonnull Class<T> annotationClass) {
    return field.getAnnotation(annotationClass);
  }
}