package io.github.tanyaofei.beancopier.test.configuration;

import io.github.tanyaofei.beancopier.BeanCopierImpl;
import io.github.tanyaofei.beancopier.NamingPolicy;
import io.github.tanyaofei.beancopier.exception.ConverterGenerateException;
import io.github.tanyaofei.beancopier.test.BeanCopierTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.function.Predicate;

/**
 * @author tanyaofei
 */
public class ConfigurationTest extends BeanCopierTest {

  @Test
  public void testSkipNull() {
    var beancopier = new BeanCopierImpl(config -> config.skipNull(true));
    var source = new ConfigurationObject().setSkippedVal("skipped");
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
    var source = new ConfigurationObject().setParentVal("parent");
    var target = beancopier.clone(source);
    assertNull(target.getParentVal());
  }

  @Test
  public void testPropertyUnsupported() {
    var beancopier = new BeanCopierImpl(config -> config.propertySupported(false));
    var source = new ConfigurationObject().setSkippedVal("skipped");
    var target = beancopier.clone(source);
    assertEquals("skipped", target.getSkippedVal());
  }

  @Test
  public void testNamingPolicy() {
    var beancopier = new BeanCopierImpl(config -> config.namingPolicy(new NamingPolicy() {
      @Override
      public @NotNull String getClassName(@NotNull Class<?> sourceClass, @NotNull Class<?> targetClass, @NotNull Predicate<String> predicate) {
        return "MyNamingPolicy";
      }
    }));

    var source = new ConfigurationObject();
    beancopier.clone(source);
    assertThrows(ConverterGenerateException.class, () -> {
      beancopier.clone(new Object());
    });
  }

}
