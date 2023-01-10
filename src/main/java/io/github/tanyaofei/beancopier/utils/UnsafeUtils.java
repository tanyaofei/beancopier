package io.github.tanyaofei.beancopier.utils;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeUtils {

  public static Unsafe getUnsafe() {
    return Lazy.theUnsafe;
  }


  private static class Lazy {

    static Unsafe theUnsafe;

    static {
      try {
        Field unsafe = Unsafe.class.getDeclaredField("theUnsafe");
        unsafe.setAccessible(true);
        theUnsafe = (Unsafe) unsafe.get(Unsafe.class);
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new IllegalStateException(e);
      }
    }

  }


}
