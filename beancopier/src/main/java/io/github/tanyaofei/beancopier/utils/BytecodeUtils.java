package io.github.tanyaofei.beancopier.utils;

import lombok.SneakyThrows;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

import javax.annotation.Nonnull;

/**
 * @author tanyaofei
 */
public class BytecodeUtils implements Opcodes {

  @SneakyThrows
  @Nonnull
  public static byte[] rename(@Nonnull Class<?> c, @Nonnull String className) {
    return rename(new ClassReader(c.getName()), className);
  }

  @SneakyThrows
  @Nonnull
  public static byte[] toBytecode(@Nonnull Class<?> c) {
    var cw = new ClassWriter(0);
    var cr = new ClassReader(c.getName());
    cr.accept(cw, 0);
    return cw.toByteArray();
  }

  @SneakyThrows
  @Nonnull
  private static byte[] rename(@Nonnull ClassReader cr, @Nonnull String className) {
    var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    var cv = new ClassRemapper(
        cw,
        new SimpleRemapper(cr.getClassName().replace(".", "/"), className.replace(".", "/"))
    );

    cr.accept(cv, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    return cw.toByteArray();
  }

  @SneakyThrows
  @Nonnull
  public static byte[] rename(@Nonnull byte[] code, @Nonnull String className) {
    return rename(new ClassReader(code), className);
  }

  @SneakyThrows
  @Nonnull
  public static byte[] repackage(@Nonnull Class<?> c, @Nonnull String packageName) {
    return rename(c, packageName + "." + c.getSimpleName());
  }

}
