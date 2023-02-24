package io.github.tanyaofei.beancopier.utils;

import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * @author tanyaofei
 */
@Getter
public class GenericType<T>  {

  private final Class<T> rawType;
  private final Type genericType;
  private final boolean generic;

  private GenericType(Class<T> rawType, Type genericType) {
    this.rawType = rawType;
    this.genericType = genericType;
    this.generic = rawType != genericType;
  }

  public static GenericType<?> of(Field field) {
    return new GenericType<>(field.getType(), field.getGenericType());
  }

  public static GenericType<?> of(RecordComponent rc) {
    return new GenericType<>(rc.getType(), rc.getGenericType());
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(new Object[]{this.rawType, this.genericType});
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj instanceof Class<?> c) {
      return this.rawType.equals(c);
    } else if (obj instanceof GenericType<?> g) {
      return this.rawType.equals(g.rawType) && this.genericType.equals(g.genericType);
    } else if (obj instanceof Type t) {
      return this.genericType.equals(t);
    } else {
      return false;
    }
  }

}
