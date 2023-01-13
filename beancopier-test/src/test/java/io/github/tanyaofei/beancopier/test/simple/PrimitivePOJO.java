package io.github.tanyaofei.beancopier.test.simple;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author tanyaofei
 */
@Data
@Accessors(chain = true)
public class PrimitivePOJO {
  private boolean booleanVal;
  private byte byteVal;
  private short shortVal;
  private int intVal;
  private long longVal;
  private float floatVal;
  private double doubleVal;
  private char charVal;

}
