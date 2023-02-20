package io.github.tanyaofei.beancopier.test.configuration.skipnull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author tanyaofei
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class SkipNullPojo {

  private String name = "name";

}
