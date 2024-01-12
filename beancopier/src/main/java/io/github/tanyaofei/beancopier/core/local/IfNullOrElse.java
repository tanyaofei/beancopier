package io.github.tanyaofei.beancopier.core.local;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * A tool for generating bytecode for the "if null" or "else" condition
 */
public class IfNullOrElse {

  /**
   * Method writer
   */
  @NotNull
  private final MethodVisitor v;

  /**
   * The label of IFNONNULL
   */
  @NotNull
  private final Label ifNonNull = new Label();

  /**
   * The label of GOTO
   */
  @NotNull
  private final Label elseGoto = new Label();

  /**
   * A runnable for generating bytecode to get a value that used to check if it is null or not
   */
  @NotNull
  private final Runnable who;

  /**
   * A runnable for generating bytecode for the case when the value({@link #who}) is null
   */
  @NotNull
  private final Runnable onNull;

  /**
   * A runnable for generating bytecode for the case when the value({@link #who}) is not null
   */
  @NotNull
  private final Runnable onNonnull;

  /**
   * Return an instance
   *
   * @param v         Method writer
   * @param who       A runnable for generating bytecode to get a value that used to check if it is null or not
   * @param onNull    A runnable for generating bytecode for the case when the value({@link #who}) is null
   * @param onNonnull A runnable for generating bytecode for the case when the value({@link #who}) is not null
   */
  public IfNullOrElse(
      @NotNull MethodVisitor v,
      @NotNull Runnable who,
      @NotNull Runnable onNull,
      @NotNull Runnable onNonnull
  ) {
    this.v = v;
    this.who = who;
    this.onNull = onNull;
    this.onNonnull = onNonnull;
  }

  /**
   * Generating bytecode for the "if null" or "else" condition
   */
  public void write() {
    who.run();
    v.visitJumpInsn(Opcodes.IFNONNULL, ifNonNull);
    onNull.run();
    v.visitJumpInsn(Opcodes.GOTO, elseGoto);
    v.visitLabel(ifNonNull);

    who.run();
    onNonnull.run();
    v.visitLabel(elseGoto);
  }


}
