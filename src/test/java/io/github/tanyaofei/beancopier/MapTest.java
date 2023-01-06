package io.github.tanyaofei.beancopier;

import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class MapTest {

  static {
    System.setProperty(BeanCopierConfiguration.PropertyNames.CONVERTER_CLASS_DUMP_PATH, "./target");
  }

  @Test
  public void testMap() {
    A a = new A()
        .setA(new HashMap<String, Integer>() {{
          put("1", 1);
          put("2", 2);
          put("3", 3);
        }})
        .setB(new HashMap<String, Integer>() {{
          put("1", 1);
          put("2", 2);
          put("3", 3);
        }});

    B b = BeanCopier.copy(a, B.class);

    Assertions.assertEquals(a.getA(), b.getA());
    Assertions.assertEquals(a.getB(), b.getB());
  }

  @Data
  @Accessors(chain = true)
  public static class A {
    private Map<String, Integer> a;
    private HashMap<String, Integer> b;
  }

  @Data
  @Accessors(chain = true)
  public static class B {
    private Map<String, Integer> a;
    private Map<String, Integer> b;
  }


}
