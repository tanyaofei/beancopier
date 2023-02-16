package io.github.tanyaofei.beancopier.test.upcasting;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * @author tanyaofei
 */
@Data
@Accessors(chain = true)
public class UpcastingTarget {

  private Value<?> value;

  private Number number;

  private Collection<? extends Number> numbers;

}
