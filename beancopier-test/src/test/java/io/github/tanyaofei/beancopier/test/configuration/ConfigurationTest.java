package io.github.tanyaofei.beancopier.test.configuration;

import io.github.tanyaofei.beancopier.BeanCopierImpl;
import io.github.tanyaofei.beancopier.extenstion.DumpConverterClassesExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;

/**
 * @author tanyaofei
 */
@ExtendWith(DumpConverterClassesExtension.class)
public class ConfigurationTest extends Assertions {

  @Test
  public void testSkipNull() {
    var beancopier = new BeanCopierImpl(config -> config.skipNull(true));
    var source = new ConfigurationPOJO().setSkippedVal("skipped");
    var target = beancopier.clone(source);
    assertEquals(target.getStringVal(), "stringVal");
    assertEquals(target.getIntVal(), 1);
    assertEquals(target.getNestedList(), Collections.emptyList());
    assertNull(target.getNested());
    assertTrue(target.isBooleanVal());
    assertEquals(target.getIntegerVal(), 1);
    assertNull(target.getSkippedVal());
  }

  @Test
  public void testNotIncludingSuper() {
    var beancopier = new BeanCopierImpl(config -> config.includingSuper(false));
    var source = new ConfigurationPOJO().setParentVal("parent");
    var target = beancopier.clone(source);
    assertNull(target.getParentVal());
  }

  @Test
  public void testPropertyUnsupported() {
    var beancopier = new BeanCopierImpl(config -> config.propertySupported(false));
    var source = new ConfigurationPOJO().setSkippedVal("skipped");
    var target = beancopier.clone(source);
    assertEquals("skipped", target.getSkippedVal());
  }

}
