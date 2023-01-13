package io.github.tanyaofei.beancopier.core.local;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author tanyaofei
 */
public class IfNonNull {

  /**
   * 方法编写器
   */
  private final MethodVisitor v;


  private final Label ifNull = new Label();

  /**
   * 定义一个变量的方法
   */
  private final Runnable localDefiner;

  /**
   * 如果 {@link #localDefiner} 不为 null 时的操作
   */
  private final Runnable onNonNull;


  public IfNonNull(MethodVisitor v, Runnable localDefiner, Runnable onNonNull) {
    this.v = v;
    this.localDefiner = localDefiner;
    this.onNonNull = onNonNull;
  }

  public void write() {
    localDefiner.run();
    v.visitJumpInsn(Opcodes.IFNULL, ifNull);
    localDefiner.run();
    onNonNull.run();
    v.visitLabel(ifNull);
  }

}
