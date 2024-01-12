package io.github.tanyaofei.beancopier.utils;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * @author tanyaofei
 */
@Getter
public class GenericType<T>  {

  @NotNull
  private final Class<T> rawType;

  @NotNull
  private final Type genericType;

  private final boolean generic;

  private GenericType(@NotNull Class<T> rawType, @NotNull Type genericType) {
    this.rawType = rawType;
    this.genericType = genericType;
    this.generic = rawType != genericType;
  }

  @NotNull
  public static GenericType<?> of(@NotNull Field field) {
    return new GenericType<>(field.getType(), field.getGenericType());
  }

//  @NotNull
//  public static GenericType<?> of(@NotNull RecordComponent rc) {
//    return new GenericType<>(rc.getType(), rc.getGenericType());
//  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(new Object[]{this.rawType, this.genericType});
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj instanceof Class<?>) {
      var c = (Class<?>) obj;
      return this.rawType.equals(c);
    } else if (obj instanceof GenericType<?>) {
      var g = (GenericType<?>) obj;
      return this.rawType.equals(g.rawType) && this.genericType.equals(g.genericType);
    } else if (obj instanceof Type) {
      var t = (Type) obj;
      return this.genericType.equals(t);
    } else {
      return false;
    }
  }

}
