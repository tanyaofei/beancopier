package io.github.tanyaofei.beancopier.test.simple;

import lombok.Data;

/**
 * @author tanyaofei
 */
@Data
public class AllArgsConstructorPOJO {

  private String a;

  private Integer b;

  private String c;

  public AllArgsConstructorPOJO(String a, Integer b, String c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }
}
