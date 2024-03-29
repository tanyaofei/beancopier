package io.github.tanyaofei.beancopier.core.local;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A tool for generating bytecode for the "if null" condition
 *
 * @author tanyaofei
 */
public class IfNonNull {

  /**
   * Method writer
   */
  private final MethodVisitor v;


  /**
   * The label of IFNUll
   */
  private final Label ifNull = new Label();

  /**
   * A runnable for generating bytecode to get a value that used to check if it is null or not
   */
  private final Runnable who;

  /**
   * A runnable for generating bytecode for the case when the value({@link #who}) is not null
   */
  private final Runnable onNonNull;


  /**
   * Return an instance
   *
   * @param v         Method writer
   * @param who       A runnable for generating bytecode to get a value that used to check if it is null or not
   * @param onNonNull A runnable for generating bytecode for the case when the value({@link #who}) is not null
   */
  public IfNonNull(MethodVisitor v, Runnable who, Runnable onNonNull) {
    this.v = v;
    this.who = who;
    this.onNonNull = onNonNull;
  }

  /**
   * Generating bytecode for the "if null" condition
   */
  public void write() {
    who.run();
    v.visitJumpInsn(Opcodes.IFNULL, ifNull);
    who.run();
    onNonNull.run();
    v.visitLabel(ifNull);
  }

}
