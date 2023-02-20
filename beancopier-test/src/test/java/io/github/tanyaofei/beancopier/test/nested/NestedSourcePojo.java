package io.github.tanyaofei.beancopier.test.nested;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author tanyaofei
 */
@Data
@Accessors(chain = true)
public class NestedSourcePojo {
  private Integer id;
  private NestedSourcePojo child;
  private List<NestedSourcePojo> children;
}
