package io.github.tanyaofei.beancopier.test.configuration.skipnull;

import io.github.tanyaofei.beancopier.BeanCopierImpl;
import io.github.tanyaofei.beancopier.extenstion.BeanCopierDebugExecution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author tanyaofei
 */
@ExtendWith(BeanCopierDebugExecution.class)
public class SkipNullTest extends Assertions {

  BeanCopierImpl beancopier = new BeanCopierImpl(config -> config.skipNull(true));

  @Test
  public void testSkipNull() {
    var source = new SkipNullPojo(null);
    var target = beancopier.clone(source);
    assertEquals("name", target.getName());
  }

//  @Test
//  public void testNotSkipNullOnRecord() {
//    var source = new SkipNullRecord(1, null);
//    var target = beancopier.clone(source);
//    assertEquals(source.intVal(), target.intVal());
//    assertEquals(source.stringVal(), target.stringVal());
//  }

}
