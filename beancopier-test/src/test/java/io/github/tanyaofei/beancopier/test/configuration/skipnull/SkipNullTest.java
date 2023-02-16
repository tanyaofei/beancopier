package io.github.tanyaofei.beancopier.test.configuration.skipnull;

import io.github.tanyaofei.beancopier.BeanCopierImpl;
import io.github.tanyaofei.beancopier.test.BeanCopierTest;
import org.junit.jupiter.api.Test;

/**
 * @author tanyaofei
 */
public class SkipNullTest extends BeanCopierTest {

  @Test
  public void testSkipNull() {
    var source = new SkipNullObject(null);
    var target = new BeanCopierImpl(config -> config.skipNull(true)).clone(source);
    assertEquals("name", target.getName());
  }

}
