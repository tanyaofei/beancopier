package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.util.DumpConverterClasses;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(DumpConverterClasses.class)
public class UnloadClassesTest {

  @Test
  public void testUnloadClasses() throws NoSuchFieldException, IllegalAccessException {
    BeanCopierImpl beanCopier = new BeanCopierImpl();
    beanCopier.copy(new Object(), Object.class);

    Reference<Class<?>> ref = new WeakReference<>(getCaches(beanCopier).values().iterator().next().getClass());
    assertFalse(getReservedClassNames().isEmpty());

    // note:
    //    beanCopier = null 就会释放 BeanCopierImpl 的引用
    //    因此 BeanCopierImpl 的 ConverterFactory 将会被释放
    //    随后 ConverterFactory 的 BeanCopierClassLoader 也将会被释放
    //    最后 BeanCopierClassLoader 加载的类也会被释放
    beanCopier = null;
    System.gc();

    getReservedClassNames().clear();
    assertNull(ref.get());
    assertTrue(getReservedClassNames().isEmpty());
  }

  @SuppressWarnings("unchecked")
  private Map<String, ? super Converter<?, ?>> getCaches(BeanCopierImpl copier) throws NoSuchFieldException, IllegalAccessException {
    Field cacheField = copier.getClass().getDeclaredField("caches");
    cacheField.setAccessible(true);
    return (Map<String, ? super Converter<?, ?>>) cacheField.get(copier);
  }

  @SuppressWarnings("unchecked")
  private Map<ClassLoader, String> getReservedClassNames() throws NoSuchFieldException, IllegalAccessException {
    Field reservedClassNamesField = ConverterFactory.class.getDeclaredField("classLoaderReservedClassNames");
    reservedClassNamesField.setAccessible(true);
    return (Map<ClassLoader, String>) reservedClassNamesField.get(ConverterFactory.class);
  }

}
