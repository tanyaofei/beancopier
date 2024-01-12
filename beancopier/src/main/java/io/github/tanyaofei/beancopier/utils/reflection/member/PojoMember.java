package io.github.tanyaofei.beancopier.utils.reflection.member;

import io.github.tanyaofei.beancopier.utils.GenericType;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author tanyaofei
 */
@Accessors
@EqualsAndHashCode
public class PojoMember implements BeanMember {

  @NotNull
  private final Field field;

  @NotNull
  private final Method method;

  @NotNull
  private final GenericType<?> genericType;

  public PojoMember(@NotNull Field field, @NotNull Method method) {
    this.field = field;
    this.method = method;
    this.genericType = GenericType.of(field);
  }

  @Override
  @NotNull
  public Object getIdentify() {
    return field;
  }

  @Override
  @NotNull
  public String getName() {
    return field.getName();
  }

  @Override
  @NotNull
  public GenericType<?> getType() {
    return this.genericType;
  }

  @Override
  @NotNull
  public Method getMethod() {
    return method;
  }

  @Override
  @Nullable
  public <T extends Annotation> T getAnnotation(@NotNull Class<T> annotationClass) {
    return field.getAnnotation(annotationClass);
  }
}