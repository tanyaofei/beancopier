package io.github.tanyaofei.beancopier.exception;


import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.BeanCopierConfiguration;
import io.github.tanyaofei.beancopier.util.DumpConverterClasses;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DumpConverterClasses.class)
public class ExceptionTest {

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
