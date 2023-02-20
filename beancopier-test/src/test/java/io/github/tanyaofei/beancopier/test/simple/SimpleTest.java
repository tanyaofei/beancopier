package io.github.tanyaofei.beancopier.test.simple;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.extenstion.DumpConverterClassesExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;

/**
 * @author tanyaofei
 */
@ExtendWith(DumpConverterClassesExtension.class)
public class SimpleTest extends Assertions {

  @Test
  public void testSimpleObject() {
    var source = new SimplePojo()
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
    var source = new PrimitivePojo()
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
  public void testSimpleRecord() {
    var source = new SimpleRecord(
        true,
        (byte) 1,
        (short) 1,
        1,
        1L,
        1F,
        1D,
        'c',
        "string",
        LocalDateTime.now()
    );

    var target = BeanCopier.clone(source);
    assertEquals(source, target);
  }

  @Test
  public void testPrimitiveRecord() {
    var source = new PrimitiveRecord(
        true,
        (byte) 1,
        (short) 1,
        1,
        1L,
        1,
        1,
        'c'
    );

    var target = BeanCopier.clone(source);
    assertEquals(source, target);
  }

  @Test
  public void testObjectToRecord() {
    var source = new SimplePojo()
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

    var target = BeanCopier.copy(source, SimpleRecord.class);
    assertEquals(source.getByteVal(), target.byteVal());
    assertEquals(source.getShortVal(), target.shortVal());
    assertEquals(source.getIntVal(), target.intVal());
    assertEquals(source.getLongVal(), target.longVal());
    assertEquals(source.getFloatVal(), target.floatVal());
    assertEquals(source.getDoubleVal(), target.doubleVal());
    assertEquals(source.getBooleanVal(), target.booleanVal());
    assertEquals(source.getStringVal(), target.stringVal());
    assertEquals(source.getCharVal(), target.charVal());
    assertEquals(source.getLocalDateTimeVal(), target.localDateTimeVal());
  }

  @Test
  public void testRecordToObject() {
    var source = new PrimitiveRecord(
        true,
        (byte) 1,
        (short) 1,
        1,
        1L,
        1,
        1,
        'c'
    );

    var target = BeanCopier.copy(source, PrimitivePojo.class);
    assertEquals(source.booleanVal(), target.isBooleanVal());
    assertEquals(source.byteVal(), target.getByteVal());
    assertEquals(source.shortVal(), target.getShortVal());
    assertEquals(source.intVal(), target.getIntVal());
    assertEquals(source.longVal(), target.getLongVal());
    assertEquals(source.floatVal(), target.getFloatVal());
    assertEquals(source.doubleVal(), target.getDoubleVal());
    assertEquals(source.charVal(), target.getCharVal());
  }


  @Test
  public void testObjectNewInstanceByAllArgsConstructor() {
    var source = new AllArgsConstructorPojo("1", 1, "c");
    var target = BeanCopier.clone(source);
    assertEquals(source.getA(), target.getA());
    assertEquals(source.getB(), target.getB());
    assertEquals(source.getC(), target.getC());
    assertDoesNotThrow(() -> {
      BeanCopier.clone(source);
    });
  }

}
