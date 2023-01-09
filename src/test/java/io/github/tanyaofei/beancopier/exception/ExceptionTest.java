package io.github.tanyaofei.beancopier.exception;


import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.BeanCopierConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExceptionTest {

  static {
    System.setProperty(BeanCopierConfiguration.PropertyNames.CONVERTER_CLASS_DUMP_PATH, "./target");
  }

  @Test
  public void testGenerateException() {
    UnPublicObject o = new UnPublicObject();
    Assertions.assertThrows(ConverterGenerateException.class, () -> {
      BeanCopier.clone(o);
    });
  }


  private static class UnPublicObject {

  }

}
