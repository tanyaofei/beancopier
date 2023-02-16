package io.github.tanyaofei.beancopier.test.nested;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.test.BeanCopierTest;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * @author tanyaofei
 */
public class NestedTest extends BeanCopierTest {

  @Test
  public void testObject() {
    var source = new NestedPOJO()
        .setSeniority(1)
        .setChild(new NestedPOJO().setSeniority(2)
                                  .setChildren(Arrays.asList(new NestedPOJO().setSeniority(3), new NestedPOJO().setSeniority(3))));

    var target = BeanCopier.clone(source);
    assertEquals(source, target);
  }

}
