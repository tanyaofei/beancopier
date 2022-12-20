package io.github.tanyaofei.beancopier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
        .setI(1)
        .setJ(true);
    source.setPa("pa");

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
    assertEquals(source.isJ(), target.isJ());
    assertNull(target.getZ());

    assertEquals(source.getPa(), target.getPa());
  }

  @Test
  public void testClone() {
    Source source = new Source()
            .setA("a")
            .setB(1)
            .setC(LocalDateTime.now())
            .setD(new Source().setA("a"))
            .setE(Collections.singletonList(new Source()))
            .setF(Arrays.asList("hello", "world"))
            .setG(new ArrayList<>())
            .setH(new InnerField().setA("inner field"))
            .setI(1)
            ;

    source.setPa("pa");
    Source target = BeanCopier.clone(source);
    assertEquals(source, target);

    List<Source> sources = new ArrayList<Source>(){{
      add(source);
      add(target);
    }};

    List<Source> targets = BeanCopier.cloneList(sources);
    for(int i = 0; i < sources.size(); i++) {
      assertEquals(sources.get(i), targets.get(i));
    }
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
  public static class Parent {
    private String pa;
  }

  @Data
  @Accessors(chain = true)
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  public static class Source extends Parent{

    private String a;
    private Integer b;
    private LocalDateTime c;
    private Source d;
    private List<Source> e;
    private List<String> f;
    private ArrayList<Integer> g;
    private InnerField h;
    private Integer i;
    private boolean j;
  }

  @Data
  @Accessors(chain = true)
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  public static class Target extends Parent{

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
    private boolean j;
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
