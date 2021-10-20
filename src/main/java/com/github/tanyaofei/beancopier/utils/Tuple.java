package com.github.tanyaofei.beancopier.utils;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author tanyaofei
 * @since 2021.08.0
 */
@Data
@Accessors(chain = true)
public class Tuple<T1, T2> {

  private T1 t1;

  private T2 t2;

  public static <T1, T2> Tuple<T1, T2> of(T1 t1, T2 t2) {
    return new Tuple<T1, T2>().setT1(t1).setT2(t2);
  }

  public static void main(String[] args) {
    var a = Stream.of(1, 2, 3, 4, 5, 6, 7);
    var b = a.filter(v -> v > 1);
    var c = b.peek(System.out::println);
    var d = c.collect(Collectors.toList());
    System.out.println(a);
  }

}
