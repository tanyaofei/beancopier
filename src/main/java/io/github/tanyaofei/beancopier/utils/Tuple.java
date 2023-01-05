package io.github.tanyaofei.beancopier.utils;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author tanyaofei
 * 2021.08.0
 */
@Data
@Accessors(chain = true)
public class Tuple<T1, T2> {

  private T1 t1;

  private T2 t2;

  public static <T1, T2> Tuple<T1, T2> of(T1 t1, T2 t2) {
    return new Tuple<T1, T2>().setT1(t1).setT2(t2);
  }

}
