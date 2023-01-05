package io.github.tanyaofei.beancopier.property;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.annotation.Property;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SkipTest {

  @Test
  public void testSkip() {
    A a = new A().setA("1").setB("1").setC("1");
    B b = BeanCopier.copy(a, B.class);
    assertEquals(a.getA(), b.getA());
    assertNull(b.getB());
    assertEquals(a.getC(), b.getC());
  }

  @Data
  @Accessors(chain = true)
  public static class A {
    private String a;

    private String b;


    private String c;
  }

  @Data
  @Accessors(chain = true)
  public static class B {
    private String a;

    @Property(skip = true)
    private String b;

    private String c;
  }


}
