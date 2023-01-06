package io.github.tanyaofei.beancopier.exception;


import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.BeanCopierConfiguration;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

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

  @Test
  public void testNPEException() {
    Assertions.assertThrows(CopyException.class, () -> {
      BeanCopier.copy(new NPEObjectSour().setObjs(Arrays.asList(null, null)), NPEObjectDest.class);
    });
  }


  private static class UnPublicObject {

  }

  @Data
  @Accessors(chain = true)
  public static class NPEObjectSour {
    private List<NPEObjectSour> objs;
  }

  @Data
  @Accessors(chain = true)
  public static class NPEObjectDest {
    private List<NPEObjectDest> objs;
  }


}
