package io.github.tanyaofei.beancopier.test.nested;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.test.BeanCopierTest;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author tanyaofei
 */
public class NestedTest extends BeanCopierTest {

  @Test
  public void testObject() {
    var source = new NestedPOJO()
        .setSeniority(1)
        .setChild(new NestedPOJO().setSeniority(2)
                                  .setChildren(List.of(new NestedPOJO().setSeniority(3), new NestedPOJO().setSeniority(3))));

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
            List.of(new NestedRecord(3, null, null), new NestedRecord(3, null, null))),
        null);
    var target = BeanCopier.clone(source);
    assertEquals(source, target);
  }


}
