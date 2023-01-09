package io.github.tanyaofei.beancopier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;

import java.io.IOException;

public class DifferenceClassLoadersTest {

  @Test
  public void test() throws IOException {
    XCLassLoader classLoader1 = new XCLassLoader();
    XCLassLoader classLoader2 = new XCLassLoader();

    Class<?> c1 = renameClass(TemplateObject.class, classLoader1, Type.getInternalName(TemplateObject.class) + "1");
    Class<?> c2 = renameClass(TemplateObject.class, classLoader2, Type.getInternalName(TemplateObject.class) + "2");

    Assertions.assertDoesNotThrow(() -> {
      BeanCopier.copy(new TemplateObject(), c1);
    });

    Assertions.assertThrows(NoClassDefFoundError.class, () -> {
      new BeanCopierImpl().copy(new TemplateObject(), c1);
      BeanCopier.copy(BeanCopier.copy(new TemplateObject(), c1), c2);
    });
  }

  public Class<?> renameClass(Class<?> c, XCLassLoader cLassLoader, String name) throws IOException {
    ClassReader cr = new ClassReader(TemplateObject.class.getName());
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

    Remapper remapper = new SimpleRemapper(Type.getInternalName(TemplateObject.class), name);
    ClassVisitor cv = new ClassRemapper(cw, remapper);

    cr.accept(cv, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    byte[] code = cw.toByteArray();
    return cLassLoader.defineClass(code);
  }

  private static class XCLassLoader extends ClassLoader {
    public Class<?> defineClass(byte[] code) {
      return super.defineClass(null, code, 0, code.length);
    }
  }


}
