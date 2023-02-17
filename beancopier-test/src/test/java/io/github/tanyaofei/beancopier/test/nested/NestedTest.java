package io.github.tanyaofei.beancopier.test.nested;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.extenstion.DumpConverterClassesExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

/**
 * @author tanyaofei
 */
@ExtendWith(DumpConverterClassesExtension.class)
public class NestedTest extends Assertions {

  @Test
  public void testObject() {
    var source = new NestedPOJO()
        .setSeniority(1)
        .setChild(new NestedPOJO().setSeniority(2)
                                  .setChildren(List.of(
                                      new NestedPOJO().setSeniority(3),
                                      new NestedPOJO().setSeniority(3)
                                  )));

    var target = BeanCopier.clone(source);
    assertEquals(source, target);
  }

  @Test
  public void testRecord() {
    var source = new NestedRecord(
        1,
        new NestedRecord(
            2,
            null,
            List.of(new NestedRecord(3, null, null), new NestedRecord(3, null, null))
        ),
        null
    );
    var target = BeanCopier.clone(source);
    assertEquals(source, target);
  }

  @Test
  public void testPOJOToRecord() {
    var source = new NestedPOJO()
        .setSeniority(1)
        .setChild(new NestedPOJO().setSeniority(2)
                                  .setChildren(List.of(
                                      new NestedPOJO().setSeniority(3),
                                      new NestedPOJO().setSeniority(3)
                                  )));

    var target = BeanCopier.copy(source, NestedRecord.class);
    var target2 = BeanCopier.copy(target, NestedPOJO.class);
    assertEquals(source, target2);
  }


}
