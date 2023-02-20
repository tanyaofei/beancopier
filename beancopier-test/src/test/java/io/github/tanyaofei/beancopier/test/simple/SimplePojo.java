package io.github.tanyaofei.beancopier.test.simple;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author tanyaofei
 */
@Data
@Accessors(chain = true)
public class SimplePojo {

  private Boolean booleanVal;

  private Byte byteVal;

  private Short shortVal;

  private Integer intVal;

  private Long longVal;

  private Float floatVal;

  private Double doubleVal;

  private Character charVal;

  private String stringVal;

  private LocalDateTime localDateTimeVal;

}
