package io.github.tanyaofei.beancopier.utils;

import lombok.SneakyThrows;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

public class BytecodeUtils implements Opcodes {

  @SneakyThrows
  public static byte[] rename(Class<?> c, String className) {
    var cr = new ClassReader(c.getName());
    var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    var cv = new ClassRemapper(cw, new SimpleRemapper(Type.getInternalName(c), className.replace(".", "/")));

    cr.accept(cv, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    return cw.toByteArray();
  }

  @SneakyThrows
  public static byte[] repackage(Class<?> c, String packageName) {
    return rename(c, packageName + "." + c.getSimpleName());
  }

}
