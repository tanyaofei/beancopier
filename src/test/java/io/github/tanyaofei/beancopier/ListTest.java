package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.util.DumpConverterClasses;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(DumpConverterClasses.class)
public class ListTest {

  @Test
  public void testList() {
    ArrayList<String> stringList = new ArrayList<>();
    stringList.add("1");
    stringList.add("2");
    stringList.add("3");

    ArrayList<A> aList = new ArrayList<>();
    aList.add(new A().setA(Arrays.asList("1", "2", "3")));
    aList.add(new A().setA(Arrays.asList("10", "20", "30")));
    aList.add(new A().setA(Arrays.asList("100", "200", "300")));

    A a = new A()
        .setA(stringList)
        .setB(stringList)
        .setC(aList)
        .setD(aList);
    B b = BeanCopier.copy(a, B.class);

    assertEquals(a.getA(), b.getA());
    assertEquals(a.getB(), b.getB());

    assertEquals(a.getC().size(), b.getC().size());
    for (int i = 0; i < a.getC().size(); i++) {
      A a1 = a.getC().get(i);
      B b1 = b.getC().get(i);
      assertEquals(a1.getA(), b1.getA());
    }

    assertEquals(a.getD().size(), b.getD().size());
    for (int i = 0; i < a.getD().size(); i++) {
      A a1 = a.getD().get(i);
      B b1 = b.getD().get(i);
      assertEquals(a1.getA(), b1.getA());
    }

  }

  @Data
  @Accessors(chain = true)
  public static class A {
    private List<String> a;
    private ArrayList<String> b;
    private List<A> c;
    private ArrayList<A> d;
  }

  @Data
  @Accessors(chain = true)
  public static class B {
    private List<String> a;
    private List<String> b;
    private List<B> c;
    private List<B> d;
  }


}
