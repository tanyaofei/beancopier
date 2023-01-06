package io.github.tanyaofei.beancopier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Map;

public class UnloadClassesTest {

  static {
    System.setProperty(BeanCopierConfiguration.PropertyNames.CONVERTER_CLASS_DUMP_PATH, "./target");
  }

  @Test
  public void testUnloadClasses() throws NoSuchFieldException, IllegalAccessException {
    BeanCopierImpl beanCopier = new BeanCopierImpl();
    beanCopier.copy(new Object(), Object.class);

    Reference<Class<?>> ref = new WeakReference<>(getCaches(beanCopier).values().iterator().next().getClass());

    // note:
    //    beanCopier = null 就会释放 BeanCopierImpl 的引用
    //    因此 BeanCopierImpl 的 ConverterFactory 将会被释放
    //    随后 ConverterFactory 的 BeanCopierClassLoader 也将会被释放
    //    最后 BeanCopierClassLoader 加载的类也会被释放
    beanCopier = null;
    System.gc();

    Assertions.assertNull(ref.get());
  }

  @SuppressWarnings("unchecked")
  private Map<String, ? super Converter<?, ?>> getCaches(BeanCopierImpl copier) throws NoSuchFieldException, IllegalAccessException {
    Field cacheField = copier.getClass().getDeclaredField("caches");
    cacheField.setAccessible(true);
    return (Map<String, ? super Converter<?, ?>>) cacheField.get(copier);
  }

}
