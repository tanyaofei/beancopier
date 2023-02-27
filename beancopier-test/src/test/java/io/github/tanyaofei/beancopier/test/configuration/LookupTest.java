package io.github.tanyaofei.beancopier.test.configuration;

import io.github.tanyaofei.beancopier.BeanCopierImpl;
import io.github.tanyaofei.beancopier.LookupUtils;
import io.github.tanyaofei.beancopier.test.util.TemplateObject;
import io.github.tanyaofei.beancopier.utils.BytecodeUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author tanyaofei
 */
public class LookupTest extends Assertions {


  @Test
  public void testLookup() {
    var classloader = new XClassLoader();
    var lookup = LookupUtils.lookupInClassLoader(classloader, XClassLoader::defineClass);
    assertDoesNotThrow(() -> {
      LookupUtils.lookupInClassLoader(classloader, XClassLoader::defineClass);
      LookupUtils.lookupInClassLoader(classloader, XClassLoader::defineClass);
    });
    var beancopier = new BeanCopierImpl(config -> {
      config.lookup(lookup);
    });

    var clazz = classloader.defineClass(BytecodeUtils.rename(TemplateObject.class, this.getClass().getPackageName() + ".Object"));
    assertDoesNotThrow(() -> {
      beancopier.copy(clazz.getConstructor().newInstance(), Object.class);
    });
  }

  private static class XClassLoader extends ClassLoader {
    public Class<?> defineClass(byte[] code) {
      return super.defineClass(null, code, 0, code.length);
    }
  }

}
