package io.github.tanyaofei.beancopier.test.simple;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.test.BeanCopierTest;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * @author tanyaofei
 */
public class SimpleTest extends BeanCopierTest {

  @Test
  public void testSimpleObject() {
    var source = new SimplePOJO()
        .setByteVal((byte) 1)
        .setShortVal((short) 1)
        .setIntVal(1)
        .setLongVal(1L)
        .setFloatVal(1F)
        .setDoubleVal(1D)
        .setBooleanVal(true)
        .setStringVal("string")
        .setCharVal('c')
        .setLocalDateTimeVal(LocalDateTime.now());

    var target = BeanCopier.clone(source);
    assertEquals(source, target);
  }

  @Test
  public void testPrimitiveObject() {
    var source = new PrimitivePOJO()
        .setByteVal((byte) 1)
        .setShortVal((short) 1)
        .setIntVal(1)
        .setLongVal(1L)
        .setFloatVal(1F)
        .setDoubleVal(1D)
        .setBooleanVal(true)
        .setCharVal('c');
    var target = BeanCopier.clone(source);
    assertEquals(source, target);
  }


  @Test
  public void testObjectNewInstanceByAllArgsConstructor() {
    var source = new AllArgsConstructorPOJO("1", 1, "c");
    assertDoesNotThrow(() -> {
      BeanCopier.clone(source);
    });
  }

}
