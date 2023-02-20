package io.github.tanyaofei.beancopier.test.upcasting;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author tanyaofei
 */
@Data
@Accessors(chain = true)
public class UpcastingSource {

  private StringValue value;

  private Integer number;

  private List<Integer> numbers;

}
