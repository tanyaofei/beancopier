package io.github.tanyaofei.beancopier.utils;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author tanyaofei
 * 2021.08.0
 */
@Data
@Accessors(chain = true)
public class Pair<X, Y> {

  private X x;

  private Y y;

  public static <X, Y> Pair<X, Y> of(X x, Y y) {
    return new Pair<X, Y>().setX(x).setY(y);
  }

}
