package io.github.tanyaofei.beancopier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author tanyaofei
 */
public class BeanCopierTest extends Assertions {

  @Test
  public void testCopy() {
    Source source = new Source()
        .setA("a")
        .setB(1)
        .setC(LocalDateTime.now())
        .setD(new Source().setA("a"))
        .setE(Collections.singletonList(new Source()))
        .setF(Arrays.asList("hello", "world"))
        .setG(new ArrayList<>())
        .setH(new InnerField().setA("inner field"))
        .setI(1);

    Target target = BeanCopier.copy(source, Target.class);
    assertEquals(source.getA(), target.getA());
    assertNull(target.getB());
    assertEquals(source.getC(), target.getC());
    assertEquals(source.getD().getA(), target.getD().getA());
    assertEquals(source.getE().get(0).getA(), target.getE().get(0).getA());
    assertEquals(source.getF(), target.getF());
    assertEquals(source.getG(), target.getG());
    assertEquals(source.getH(), target.getH());
    assertEquals(source.getI(), target.getI());
    assertNull(target.getZ());
  }

  @Test
  public void testUnPublicCopy() {
    Source source = new Source();
    try {
      UnPublicTarget target = BeanCopier.copy(source, UnPublicTarget.class);
    } catch (IllegalArgumentException exception) {
      // ignored
      return;
    }
    throw new IllegalStateException();
  }

  @Data
  @Accessors(chain = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Source {

    private String a;
    private Integer b;
    private LocalDateTime c;
    private Source d;
    private List<Source> e;
    private List<String> f;
    private ArrayList<Integer> g;
    private InnerField h;
    private Integer i;
  }

  @Data
  @Accessors(chain = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Target {

    private String a;         // ok
    private String b;         // null
    private LocalDateTime c;  // ok
    private Target d;         // ok
    private List<Target> e;   // ok
    private List<String> f;   // ok
    private List<Integer> g;  // ok
    private InnerField h;     // ok
    private Number i;         // ok
    private Object z;         // null
  }

  @Data
  @Accessors(chain = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public static class InnerField {

    private String a;
  }

  private static class UnPublicTarget {

  }

}
