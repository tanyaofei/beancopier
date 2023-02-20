package io.github.tanyaofei.beancopier.test.exception;


import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.exception.ConverterGenerateException;
import io.github.tanyaofei.beancopier.exception.CopyException;
import io.github.tanyaofei.beancopier.extenstion.DumpConverterClassesExtension;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DumpConverterClassesExtension.class)
public class ExceptionTest extends Assertions {

  @Test
  public void testDuringCopying() {
    assertThrows(
        CopyException.class,
        () -> BeanCopier.copy(new ExceptionOnGetter(), ExceptionOnGetter.class)
    ).printStackTrace();
  }

  @Test
  public void testEnclosing() {
    assertThrows(
        ConverterGenerateException.class,
        () -> BeanCopier.copy(new Object(), EnclosingObject.class)
    ).printStackTrace();
    assertDoesNotThrow(() -> {
      BeanCopier.copy(new EnclosingObject(), Object.class);
    });
  }

  @Test
  public void testPublic() {
    assertThrows(ConverterGenerateException.class, () -> BeanCopier.clone(new UnPublicObject())).printStackTrace();
  }

  public static class ExceptionOnGetter {
    private String val;

    public String getVal() {
      if (true) {
        throw new RuntimeException();
      }
      return "";
    }

    public void setVal(String val) {
      throw new RuntimeException();
    }

  }

  @Test
  public void testAbstract() {
    assertThrows(ConverterGenerateException.class, () -> BeanCopier.copy(new Object(), AbstractObject.class)).printStackTrace();
  }

  @Test
  public void testEnum() {
    assertThrows(ConverterGenerateException.class, () -> BeanCopier.copy(new Object(), EnumObj.class)).printStackTrace();
  }

  @Test
  public void testInterface() {
    assertThrows(ConverterGenerateException.class, () -> BeanCopier.copy(new Object(), InterfaceObject.class)).printStackTrace();
  }

  @Test
  public void testPrimitive() {
    assertThrows(ConverterGenerateException.class, () -> BeanCopier.copy(new Object(), int.class)).printStackTrace();
    assertThrows(ConverterGenerateException.class, () -> BeanCopier.copy(1, int.class)).printStackTrace();
    assertDoesNotThrow(() -> BeanCopier.copy(2, String.class));
  }

  @Test
  public void testLocal() {
    @Data
    @Accessors(chain = true)
    class LocalObject {
    }
    assertThrows(ConverterGenerateException.class, () -> BeanCopier.copy(new LocalObject(), Object.class));

    assertThrows(ConverterGenerateException.class, () -> {
      BeanCopier.copy(
          new Object(),
          LocalObject.class);
    }).printStackTrace();
  }


  private static class UnPublicObject {

  }

  public enum EnumObj {
    OK
  }

  public interface InterfaceObject {

  }

  public static abstract class AbstractObject {

  }

  public class EnclosingObject {
  }


}
