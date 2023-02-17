package io.github.tanyaofei.beancopier.utils;

import lombok.SneakyThrows;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

/**
 * @author tanyaofei
 */
public class BytecodeUtils implements Opcodes {

  @SneakyThrows
  public static byte[] rename(Class<?> c, String className) {
    return rename(new ClassReader(c.getName()), className);
  }

  @SneakyThrows
  private static byte[] rename(ClassReader cr, String className) {
    var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    var cv = new ClassRemapper(cw, new SimpleRemapper(cr.getClassName().replace(".", "/"), className.replace(".", "/")));

    cr.accept(cv, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    return cw.toByteArray();
  }

  @SneakyThrows
  public static byte[] rename(byte[] code, String className) {
    return rename(new ClassReader(code), className);
  }

  @SneakyThrows
  public static byte[] repackage(Class<?> c, String packageName) {
    return rename(c, packageName + "." + c.getSimpleName());
  }

}
