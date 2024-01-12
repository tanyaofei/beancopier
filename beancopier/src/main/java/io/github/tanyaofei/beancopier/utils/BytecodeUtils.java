package io.github.tanyaofei.beancopier.utils;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
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
  public static byte @NotNull [] rename(@NotNull Class<?> c, @NotNull String className) {
    return rename(new ClassReader(c.getName()), className);
  }

  @SneakyThrows
  public static byte @NotNull [] toBytecode(@NotNull Class<?> c) {
    var cw = new ClassWriter(0);
    var cr = new ClassReader(c.getName());
    cr.accept(cw, 0);
    return cw.toByteArray();
  }

  @SneakyThrows
  private static byte @NotNull [] rename(@NotNull ClassReader cr, @NotNull String className) {
    var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    var cv = new ClassRemapper(
        cw,
        new SimpleRemapper(cr.getClassName().replace(".", "/"), className.replace(".", "/"))
    );

    cr.accept(cv, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    return cw.toByteArray();
  }

  @SneakyThrows
  public static byte @NotNull [] rename(@NotNull byte[] code, @NotNull String className) {
    return rename(new ClassReader(code), className);
  }

  @SneakyThrows
  public static byte @NotNull [] repackage(@NotNull Class<?> c, @NotNull String packageName) {
    return rename(c, packageName + "." + c.getSimpleName());
  }

}
