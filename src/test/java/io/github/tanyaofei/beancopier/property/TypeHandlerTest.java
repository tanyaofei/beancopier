package io.github.tanyaofei.beancopier.property;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.BeanCopierConfiguration;
import io.github.tanyaofei.beancopier.annotation.Property;
import io.github.tanyaofei.beancopier.typehandler.impl.arraytolist.*;
import io.github.tanyaofei.beancopier.typehandler.impl.listtoarray.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TypeHandlerTest {

  static {
    System.setProperty(BeanCopierConfiguration.PropertyNames.CONVERTER_CLASS_DUMP_PATH, "./");
  }

  @Test
  public void testArrayToListTypeHandler() {
    A a = new A()
        .setA(new Boolean[]{true, false, true})
        .setB(new Byte[]{1, 2, 3})
        .setC(new Character[]{'1', '2', '3'})
        .setD(new Double[]{1D, 2D, 3D})
        .setE(new Float[]{1F, 2F, 3F})
        .setF(new Integer[]{1, 2, 3})
        .setG(new Long[]{1L, 2L, 3L})
        .setH(new Short[]{(short) 1, (short) 2, (short) 3})
        .setI(new String[]{"1", "2", "3"});

    B b = BeanCopier.copy(a, B.class);
    assertEquals(Arrays.asList(a.getA()), b.getA());
    assertEquals(Arrays.asList(a.getB()), b.getB());
    assertEquals(Arrays.asList(a.getC()), b.getC());
    assertEquals(Arrays.asList(a.getD()), b.getD());
    assertEquals(Arrays.asList(a.getF()), b.getF());
    assertEquals(Arrays.asList(a.getG()), b.getG());
    assertEquals(Arrays.asList(a.getH()), b.getH());
    assertEquals(Arrays.asList(a.getI()), b.getI());
    assertEquals(Arrays.asList(a.getI()), b.getI1());

    assertNull(b.getJ());


    A a1 = BeanCopier.copy(b, A.class);
    assertArrayEquals(a1.getA(), a.getA());
    assertArrayEquals(a1.getB(), a.getB());
    assertArrayEquals(a1.getC(), a.getC());
    assertArrayEquals(a1.getD(), a.getD());
    assertArrayEquals(a1.getF(), a.getF());
    assertArrayEquals(a1.getG(), a.getG());
    assertArrayEquals(a1.getH(), a.getH());
    assertArrayEquals(a1.getI(), a.getI());
  }

  @Data
  @Accessors(chain = true)
  public static class A {
    @Property(typeHandler = BooleanListToArrayTypeHandler.class)
    private Boolean[] a;
    @Property(typeHandler = ByteListToArrayTypeHandler.class)
    private Byte[] b;
    @Property(typeHandler = CharacterListToArrayTypeHandler.class)
    private Character[] c;
    @Property(typeHandler = DoubleListToArrayTypeHandler.class)
    private Double[] d;
    @Property(typeHandler = FloatListToArrayTypeHandler.class)
    private Float[] e;
    @Property(typeHandler = IntegerListToArrayTypeHandler.class)
    private Integer[] f;
    @Property(typeHandler = LongListToArrayTypeHandler.class)
    private Long[] g;
    @Property(typeHandler = ShortListToArrayTypeHandler.class)
    private Short[] h;
    @Property(typeHandler = StringListToArrayTypeHandler.class)
    private String[] i;
    @Property(typeHandler = StringListToArrayTypeHandler.class)
    private String[] j;
  }

  @Data
  @Accessors(chain = true)
  public static class B {
    @Property(typeHandler = BooleanArrayToListTypeHandler.class)
    private List<Boolean> a;
    @Property(typeHandler = ByteArrayToListTypeHandler.class)
    private List<Byte> b;
    @Property(typeHandler = CharacterArrayToListTypeHandler.class)
    private List<Character> c;
    @Property(typeHandler = DoubleArrayToListTypeHandler.class)
    private List<Double> d;
    @Property(typeHandler = FloatArrayToListTypeHandler.class)
    private List<Float> e;
    @Property(typeHandler = IntegerArrayToListTypeHandler.class)
    private List<Integer> f;
    @Property(typeHandler = LongArrayToListTypeHandler.class)
    private List<Long> g;
    @Property(typeHandler = ShortArrayToListTypeHandler.class)
    private List<Short> h;
    @Property(typeHandler = StringArrayToListTypeHandler.class)
    private List<String> i;

    @Property(typeHandler = StringArrayToListTypeHandler.class)
    private List<String> j;
    @Property(value = "i", typeHandler = StringArrayToListTypeHandler.class)
    private List<String> i1;
  }

}
