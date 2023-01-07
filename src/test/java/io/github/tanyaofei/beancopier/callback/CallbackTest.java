package io.github.tanyaofei.beancopier.callback;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.BeanCopierConfiguration;
import io.github.tanyaofei.beancopier.Callback;
import io.github.tanyaofei.beancopier.exception.ExceptionTest;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CallbackTest {

  static {
    System.setProperty(BeanCopierConfiguration.PropertyNames.CONVERTER_CLASS_DUMP_PATH, "./target");
  }

  @Test
  public void testUnbox() {
    Boxed boxed = new Boxed().setA(1).setB((short) 1);
    Unboxed unboxed = BeanCopier.copy(boxed, Unboxed.class, new UnboxingCallback());

    assertEquals(boxed.getA(), unboxed.getA());
    assertEquals(boxed.getB(), unboxed.getB());
  }

  @Test
  public void testListCallback() {
    List<Boxed> boxedList = Arrays.asList(new Boxed().setA(1), new Boxed().setA(2), null, new Boxed().setA(4));
    List<Unboxed> unboxedList = BeanCopier.copyList(boxedList, Unboxed.class, new UnboxingCallback());
    for (int i = 0; i < boxedList.size(); i++) {
      if (boxedList.get(i) == null) {
        assertNull(unboxedList.get(i));
        continue;
      }
      assertEquals(boxedList.get(i).getA(), unboxedList.get(i).getA());
    }
  }

  public static class UnboxingCallback implements Callback<Boxed, Unboxed> {

    @Override
    public void apply(Boxed source, Unboxed target) {
      if (source == null) {
        return;
      }
      if (source.getA() != null) {
        target.setA(source.getA());
      }
      if (source.getB() != null) {
        target.setB(source.getB());
      }
    }
  }

  @Data
  @Accessors(chain = true)
  public static class Boxed {
    private Integer a;
    private Short b;
  }


  @Data
  @Accessors(chain = true)
  public static class Unboxed {
    private int a;
    private short b;
  }


}
