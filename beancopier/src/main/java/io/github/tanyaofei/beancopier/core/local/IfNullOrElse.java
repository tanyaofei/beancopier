package io.github.tanyaofei.beancopier.core.local;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 用于编写 if null or else 的字节码工具
 */
public class IfNullOrElse {

  private final MethodVisitor v;
  private final Label ifNonNull = new Label();
  private final Label elseGoto = new Label();
  private final Runnable localDefiner;
  private final Runnable onNull;
  private final Runnable onNonnull;

  /**
   * 创建一个 if null or else 字节码工具
   *
   * @param v            方法编写其
   * @param localDefiner 定义一个变量的方法
   * @param onNull       如果 {@link #localDefiner} 为 null 时的操作
   * @param onNonnull    如果 {@link #localDefiner} 不为 null 时的操作
   * @since 0.2.0
   */
  public IfNullOrElse(MethodVisitor v, Runnable localDefiner, Runnable onNull, Runnable onNonnull) {
    this.v = v;
    this.localDefiner = localDefiner;
    this.onNull = onNull;
    this.onNonnull = onNonnull;
  }

  /**
   * 编写字节码
   * @since 0.2.0
   */
  public void write() {
    localDefiner.run();
    v.visitJumpInsn(Opcodes.IFNONNULL, ifNonNull);
    onNull.run();
    v.visitJumpInsn(Opcodes.GOTO, elseGoto);
    v.visitLabel(ifNonNull);

    localDefiner.run();
    onNonnull.run();
    v.visitLabel(elseGoto);
  }


}
