package io.github.tanyaofei.beancopier.constants;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.var;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author tanyaofei
 */
@AllArgsConstructor
public enum LocalOpcode {

  INT(int.class, Opcodes.ISTORE, Opcodes.ILOAD, Opcodes.ICONST_0, 1),
  SHORT(short.class, Opcodes.ISTORE, Opcodes.ILOAD, Opcodes.ICONST_0, 1),
  BOOLEAN(boolean.class, Opcodes.ISTORE, Opcodes.ILOAD, Opcodes.ICONST_0, 1),
  CHAR(char.class, Opcodes.ISTORE, Opcodes.ILOAD, Opcodes.ICONST_0, 1),
  BYTE(byte.class, Opcodes.ISTORE, Opcodes.ILOAD, Opcodes.ICONST_0, 1),
  Long(long.class, Opcodes.LSTORE, Opcodes.LLOAD, Opcodes.LCONST_0, 2),
  FLOAT(float.class, Opcodes.FSTORE, Opcodes.FLOAD, Opcodes.FCONST_0, 1),
  DOUBLE(double.class, Opcodes.DSTORE, Opcodes.DLOAD, Opcodes.DCONST_0, 2),
  REFERENCE(Object.class, Opcodes.ASTORE, Opcodes.ALOAD, Opcodes.ACONST_NULL, 1),

  INT_ARRAY(int[].class, Opcodes.IASTORE, Opcodes.IALOAD, Opcodes.ACONST_NULL, 1),
  SHORT_ARRAY(short[].class, Opcodes.SASTORE, Opcodes.SALOAD, Opcodes.ACONST_NULL, 1),
  BOOLEAN_ARRAY(boolean[].class, Opcodes.BASTORE, Opcodes.BALOAD, Opcodes.ACONST_NULL, 1),
  CHAR_ARRAY(char[].class, Opcodes.CASTORE, Opcodes.CALOAD, Opcodes.ACONST_NULL, 1),
  BYTE_ARRAY(byte[].class, Opcodes.BASTORE, Opcodes.BALOAD, Opcodes.ACONST_NULL, 1),
  LONG_ARRAY(long[].class, Opcodes.LASTORE, Opcodes.LALOAD, Opcodes.ACONST_NULL, 1),
  FLOAT_ARRAY(float[].class, Opcodes.FASTORE, Opcodes.FALOAD, Opcodes.ACONST_NULL, 1),
  DOUBLE_ARRAY(double[].class, Opcodes.DASTORE, Opcodes.DALOAD, Opcodes.ACONST_NULL, 1),
  REFERENCE_ARRAY(Object[].class, Opcodes.AASTORE, Opcodes.AALOAD, Opcodes.ACONST_NULL, 1);

  private static volatile Map<Class<?>, LocalOpcode> map;

  @Getter(AccessLevel.PRIVATE)
  public final Class<?> type;
  public final int storeOpcode;
  public final int loadOpcode;
  public final int zeroOpcode;
  public final int slots;

  /**
   * Return a LocalOpcode value of the specified type
   *
   * @param type any type
   * @return a LocalOpcode value, should not be null
   */
  public static LocalOpcode ofType(Class<?> type) {
    if (map == null) {
      synchronized (LocalOpcode.class) {
        if (map == null) {
          map = Arrays.stream(values()).collect(Collectors.toMap(LocalOpcode::getType, o -> o));
        }
      }
    }

    var opcode = map.get(type);
    if (opcode != null) {
      return opcode;
    }

    return type.isArray()
           ? REFERENCE_ARRAY
           : REFERENCE;
  }

  /**
   * Const a zero value in method
   *
   * @param v Method writer
   */
  public void constZero(MethodVisitor v) {
    v.visitInsn(this.zeroOpcode);
  }

}
