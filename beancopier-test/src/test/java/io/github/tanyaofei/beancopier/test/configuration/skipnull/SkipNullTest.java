package io.github.tanyaofei.beancopier.test.configuration.skipnull;

import io.github.tanyaofei.beancopier.BeanCopierImpl;
import io.github.tanyaofei.beancopier.extenstion.DumpConverterClassesExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author tanyaofei
 */
@ExtendWith(DumpConverterClassesExtension.class)
public class SkipNullTest extends Assertions {

  @Test
  public void testSkipNull() {
    var source = new SkipNullPOJO(null);
    var target = new BeanCopierImpl(config -> config.skipNull(true)).clone(source);
    assertEquals("name", target.getName());
  }

}
