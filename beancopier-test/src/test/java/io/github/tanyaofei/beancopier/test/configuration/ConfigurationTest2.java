package io.github.tanyaofei.beancopier.test.configuration;

import io.github.tanyaofei.beancopier.BeanCopierImpl;
import io.github.tanyaofei.beancopier.annotation.Property;
import io.github.tanyaofei.beancopier.test.util.DumpConverterClasses;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(DumpConverterClasses.class)
public class ConfigurationTest2 {

  @Test
  public void testNotSupportProperty() {
    assertNull(new BeanCopierImpl(builder -> builder.propertySupported(false).classDumpPath(DumpConverterClasses.dumpPath)).copy(new A().setA("1"), B.class).a1);
  }

  @Test
  public void testFullTypeMatching() {
    assertNull(new BeanCopierImpl(builder -> builder.fullTypeMatching(true).classDumpPath(DumpConverterClasses.dumpPath)).copy(new A().setB(1), B.class).b);
  }

  @Test
  public void testNotNestedCopy() {
    A a = new A()
        .setC(new A())
        .setD(Collections.singletonList(new A()));
    B b = new BeanCopierImpl(builder -> builder.preferNested(false).classDumpPath(DumpConverterClasses.dumpPath)).copy(a, B.class);
    assertNull(b.c);
    assertNull(b.d);
  }

  @Test
  public void testNotCopySuperProperties() {
    C c = new C();
    c.setA("1");

    D d = new BeanCopierImpl(builder -> builder.includingSuper(false).classDumpPath(DumpConverterClasses.dumpPath)).copy(c, D.class);
    assertNull(d.getA());
  }

  @Test
  public void testSkipNull() {
    A a = new A().setE(null);
    B b = new BeanCopierImpl(builder -> builder.skipNull(true).classDumpPath(DumpConverterClasses.dumpPath)).copy(a, B.class);
    assertEquals(b.getE(), "1");
  }


  @Data
  @Accessors(chain = true)
  public static class A {
    private String a;
    private Integer b;
    private A c;
    private List<A> d;
    private String e;
  }

  @Data
  @Accessors(chain = true)
  public static class B {
    @Property(value = "a")
    private String a1;
    private Number b;
    private B c;
    private List<B> d;
    private String e = "1";
  }

  @Data
  @Accessors(chain = true)
  @EqualsAndHashCode(callSuper = true)
  public static class C extends A {

  }

  @Data
  @Accessors(chain = true)
  @EqualsAndHashCode(callSuper = true)
  public static class D extends A {

  }


}
