package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.asserts.XAsserts;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author tanyaofei
 */
public class NormalTest extends Assertions {


  static {
    System.setProperty(BeanCopierConfiguration.PropertyNames.CONVERTER_CLASS_DUMP_PATH, "./target");
  }

  @Test
  public void testClone() {
    StandardSour sour = new StandardSour()
        .setA(1)
        .setB((short) 1)
        .setC(1L)
        .setD((byte) 1)
        .setE(true)
        .setF('1')
        .setG(1F)
        .setH(1D)
        .setI(LocalTime.now())
        .setJ(LocalDate.now())
        .setK(LocalDateTime.now())
        .setL(BigDecimal.ONE);
    assertEquals(sour, BeanCopier.clone(sour));
  }

  @Test
  public void testStandard() {
    StandardSour sour = new StandardSour()
        .setA(1)
        .setB((short) 1)
        .setC(1L)
        .setD((byte) 1)
        .setE(true)
        .setF('1')
        .setG(1F)
        .setH(1D)
        .setI(LocalTime.now())
        .setJ(LocalDate.now())
        .setK(LocalDateTime.now())
        .setL(BigDecimal.ONE);

    StandardDest dest = BeanCopier.copy(sour, StandardDest.class);
    assertEquals(sour.a, dest.a);
    assertEquals(sour.b, dest.b);
    assertEquals(sour.c, dest.c);
    assertEquals(sour.d, dest.d);
    assertEquals(sour.e, dest.e);
    assertEquals(sour.f, dest.f);
    assertEquals(sour.g, dest.g);
    assertEquals(sour.h, dest.h);
    assertEquals(sour.i, dest.i);
    assertEquals(sour.j, dest.j);
    assertEquals(sour.k, dest.k);
    assertEquals(sour.l, dest.l);
  }

  @Test
  public void testList() {
    ListSour sour = new ListSour().setA(Arrays.asList(1, 2, 3)).setB(Arrays.asList("1", null, "3"));

    ListDest dest = BeanCopier.copy(sour, ListDest.class);
    assertEquals(sour.a, dest.a);
    assertEquals(sour.b, dest.b);
  }

  @Test
  public void testExtends() {
    ChildA childA = new ChildA();
    childA.setA("1");
    childA.setB("1");
    childA.setC1("1");

    ChildB childB = BeanCopier.copy(childA, ChildB.class);
    assertEquals(childA.getA(), childB.getA());
    assertEquals(childA.b, childB.b);
    assertNull(childB.getC2());
  }

  @Test
  public void testNesting() {
    NestingSour sour = new NestingSour().setNestingField(new NestingSour.NestingField().setA("a"));

    NestingDest dest = BeanCopier.copy(sour, NestingDest.class);
    assertEquals(sour.getNestingField(), dest.getNestingField());
  }

  @Test
  public void testRecursion() {
    ArrayList<RecursionSour> c = new ArrayList<>();
    c.add(new RecursionSour().setD("10").setA(new RecursionSour().setD("1")));
    c.add(new RecursionSour().setD("20").setA(new RecursionSour().setD("2")));
    c.add(new RecursionSour().setD("30").setA(new RecursionSour().setD("3")));
    c.add(null);
    c.add(null);
    c.add(new RecursionSour().setD("40").setA(new RecursionSour().setD("4")));

    RecursionSour sour = new RecursionSour()
        .setA(new RecursionSour().setD("1"))
        .setB(Arrays.asList(
            new RecursionSour().setD("1"),
            new RecursionSour().setD("2"),
            new RecursionSour().setD("3"),
            null,
            null,
            new RecursionSour().setD("4")))
        .setC(c)
        .setD("1");

    RecursionDest dest = BeanCopier.copy(sour, RecursionDest.class);
    assertEquals(sour.getD(), dest.getD());
    for (int i = 0; i < sour.getB().size(); i++) {
      if (sour.getB().get(i) == null) {
        assertNull(dest.getB().get(i));
        continue;
      }
      assertEquals(sour.getB().get(i).getD(), dest.getB().get(i).getD());
      assertEquals(sour.getC().get(i).getD(), dest.getC().get(i).getD());
      assertEquals(sour.getC().get(i).getA().getD(), dest.getC().get(i).getA().getD());
    }
  }

  @Test
  public void testUpcasting() {
    ArrayList<String> b = new ArrayList<>();
    b.add("1");
    b.add("2");
    b.add("3");
    b.add(null);
    b.add("5");

    UpcastingSour sour = new UpcastingSour()
        .setA(1)
        .setB(b)
        .setC(Arrays.asList(1, 2, 3));

    UpcastingDest dest = BeanCopier.copy(sour, UpcastingDest.class);
    assertEquals(sour.a, dest.a);
    assertEquals(sour.b, dest.b);
    assertEquals(sour.c, dest.c);
  }

  @Test
  public void testNotCopy() {
    NotCopySour sour = new NotCopySour()
        .setA("1")
        .setB(Arrays.asList("1", "2", "3"))
        .setC(1)
        .setD("1");

    NotCopyDest dest = BeanCopier.copy(sour, NotCopyDest.class);
    assertNull(dest.a1);
    assertNull(dest.b);
    assertNull(dest.c);
  }

  @Test
  public void testNull() {
    StandardSour sour = null;
    @SuppressWarnings("ConstantValue")
    StandardDest dest = BeanCopier.copy(sour, StandardDest.class);
    assertNull(dest);
  }

  @Data
  @Accessors(chain = true)
  public static class UpcastingSour {
    private Integer a;
    private ArrayList<String> b;
    private List<Integer> c;
  }

  @Data
  @Accessors(chain = true)
  public static class UpcastingDest {
    private Number a;
    private List<String> b;
    private List<? extends Number> c;
  }

  @Data
  @Accessors(chain = true)
  public static class RecursionSour {
    private RecursionSour a;
    private List<RecursionSour> b;
    private ArrayList<RecursionSour> c;
    private String d;
  }

  @Data
  @Accessors(chain = true)
  public static class RecursionDest {
    private RecursionDest a;
    private List<RecursionDest> b;
    private List<RecursionSour> c;
    private String d;
  }

  @Data
  @Accessors(chain = true)
  public static class NestingSour {

    private NestingField nestingField;

    @Data
    @Accessors(chain = true)
    public static class NestingField {
      public String a;
    }
  }

  @Data
  @Accessors(chain = true)
  public static class NestingDest {
    private NestingSour.NestingField nestingField;
  }

  @Data
  @Accessors(chain = true)
  public static class StandardSour {
    private Integer a;
    private Short b;
    private Long c;
    private Byte d;
    private Boolean e;
    private Character f;
    private Float g;
    private Double h;
    private LocalTime i;
    private LocalDate j;
    private LocalDateTime k;
    private BigDecimal l;
  }

  @Data
  @Accessors(chain = true)
  public static class StandardDest {
    private Integer a;
    private Short b;
    private Long c;
    private Byte d;
    private Boolean e;
    private Character f;
    private Float g;
    private Double h;
    private LocalTime i;
    private LocalDate j;
    private LocalDateTime k;
    private BigDecimal l;
  }

  @Data
  @Accessors(chain = true)
  public static class ListSour {
    private List<Integer> a;
    private List<String> b;
  }

  @Data
  @Accessors(chain = true)
  public static class ListDest {
    private List<Integer> a;
    private List<String> b;
  }

  @Data
  @Accessors(chain = true)
  public static class Parent {
    private String a;
  }

  @Data
  @Accessors(chain = true)
  @EqualsAndHashCode(callSuper = true)
  public static class ChildA extends Parent {
    private String b;
    private String c1;
  }

  @Data
  @Accessors(chain = true)
  @EqualsAndHashCode(callSuper = true)
  public static class ChildB extends Parent {
    private String b;
    private String c2;
  }

  @Data
  @Accessors(chain = true)
  public static class NotCopySour {
    private String a;
    private List<String> b;
    private Number c;
    private String d;
  }

  @Data
  @Accessors(chain = true)
  public static class NotCopyDest {
    private String a1;
    private ArrayList<String> b;
    private Integer c;
  }

  @Test
  public void testCopyList() {
    List<StandardSour> objs = Arrays.asList(
        new StandardSour().setA(1),
        new StandardSour().setA(2),
        new StandardSour().setA(3)
    );

    assertEquals(BeanCopier.cloneList(objs), objs);
    XAsserts.assertEquals(BeanCopier.copyList(objs, StandardDest.class), objs, StandardDest::getA, StandardSour::getA);
  }

}
