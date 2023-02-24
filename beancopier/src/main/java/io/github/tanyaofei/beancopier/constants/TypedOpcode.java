package io.github.tanyaofei.beancopier.constants;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author tanyaofei
 */
@AllArgsConstructor
public enum TypedOpcode {

  INT(int.class, Opcodes.ISTORE, Opcodes.ILOAD, Opcodes.ICONST_0, 1),
  SHORT(short.class, Opcodes.ISTORE, Opcodes.ILOAD, Opcodes.ICONST_0, 1),
  BOOLEAN(boolean.class, Opcodes.ISTORE, Opcodes.ILOAD, Opcodes.ICONST_0, 1),
  CHAR(char.class, Opcodes.ISTORE, Opcodes.ILOAD, Opcodes.ICONST_0, 1),
  BYTE(byte.class, Opcodes.ISTORE, Opcodes.ILOAD, Opcodes.ICONST_0, 1),
  Long(long.class, Opcodes.LSTORE, Opcodes.LLOAD, Opcodes.LCONST_0, 2),
  FLOAT(float.class, Opcodes.FSTORE, Opcodes.FLOAD, Opcodes.FCONST_0, 1),
  DOUBLE(double.class, Opcodes.DSTORE, Opcodes.DLOAD, Opcodes.DCONST_0, 2),
  REFERENCE(Object.class, Opcodes.ASTORE, Opcodes.ALOAD, Opcodes.ACONST_NULL, 1);

  private static volatile Map<Class<?>, TypedOpcode> map;

  @Getter(AccessLevel.PRIVATE)
  public final Class<?> type;
  public final int store;
  public final int load;
  public final int constZero;
  public final int slots;

  /**
   * Return a LocalOpcode value of the specified type
   *
   * @param type any type
   * @return a LocalOpcode value, should not be null
   */
  public static TypedOpcode ofType(Class<?> type) {
    if (map == null) {
      synchronized (TypedOpcode.class) {
        if (map == null) {
          map = Arrays.stream(values()).collect(Collectors.toMap(TypedOpcode::getType, o -> o));
        }
      }
    }

    var opcode = map.get(type);
    if (opcode != null) {
      return opcode;
    }

    return REFERENCE;
  }

  /**
   * Const a zero value in method
   *
   * @param v Method writer
   */
  public void constZero(MethodVisitor v) {
    v.visitInsn(this.constZero);
  }

}
