package io.github.tanyaofei.beancopier.test.upcasting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author tanyaofei
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class Value<T> {

  private T value;


}
