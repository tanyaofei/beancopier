package io.github.tanyaofei.beancopier.util;

import org.junit.jupiter.api.Assertions;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

public class XAsserts {

  public static <T, U, V> void assertEquals(
      Collection<T> c1,
      Collection<U> c2,
      Function<T, V> f1,
      Function<U, V> f2
  ) {
    Assertions.assertEquals(c1.size(), c2.size());
    Iterator<T> itr1 = c1.iterator();
    Iterator<U> itr2 = c2.iterator();

    while (itr1.hasNext()) {
      Assertions.assertEquals(f1.apply(itr1.next()), f2.apply(itr2.next()));
    }
  }

}
