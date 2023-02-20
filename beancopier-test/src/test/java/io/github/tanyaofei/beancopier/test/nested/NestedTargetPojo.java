package io.github.tanyaofei.beancopier.test.nested;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * @author tanyaofei
 */
@Data
@Accessors(chain = true)
public class NestedTargetPojo {
  private Integer id;
  private NestedTargetPojo child;
  private Collection<NestedTargetPojo> children;
}
