package io.github.tanyaofei.beancopier;

import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class GenericTest {

  static {
    System.setProperty(BeanCopierConfiguration.PropertyNames.CONVERTER_CLASS_DUMP_PATH, "./");
  }

  @Test
  public void testGeneric() {
    StringContainer stringContainer = new StringContainer();
    stringContainer.setVal("1");
    stringContainer.setVal2("2");

    IntegerContainer integerContainer = new IntegerContainer();
    integerContainer.setVal(1);
    integerContainer.setVal2(2);

    IntegerStringContainer integerStringContainer = new IntegerStringContainer();
    integerStringContainer.setVal(1);
    integerStringContainer.setVal2("2");

    A a = new A()
        .setA(stringContainer)
        .setB(integerContainer)
        .setC(integerStringContainer)
        .setD(integerStringContainer);
    B b = BeanCopier.copy(a, B.class);

    assertEquals(a.getA(), b.getA());
    assertEquals(a.getC(), b.getC());
    assertNull(b.getB());
    assertNull(b.getD());
  }

  public static class IntegerStringContainer extends Container<Integer, String> {

  }


  public static class IntegerContainer extends Container<Integer, Integer> {

  }

  public static class StringContainer extends Container<String, String> {

  }

  @Data
  @Accessors(chain = true)
  public static class Container<T, U> {

    private T val;
    private U val2;

  }

  @Data
  @Accessors(chain = true)
  public static class A {
    private StringContainer a;
    private IntegerContainer b;
    private IntegerStringContainer c;
    private IntegerStringContainer d;
  }

  @Data
  @Accessors(chain = true)
  public static class B {
    private Container<String, String> a;
    private Container<String, String> b;
    private Container<Integer, String> c;
    private Container<Integer, Integer> d;
  }

}
