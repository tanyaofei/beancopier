package io.github.tanyaofei.beancopier.test.nested;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author tanyaofei
 */
@Data
@Accessors(chain = true)
public class NestedPOJO {
  private int seniority;
  private NestedPOJO child;
  private List<NestedPOJO> children;

}
