package io.github.tanyaofei.beancopier.test.configuration;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author tanyaofei
 */
@Data
@Accessors(chain = true)
public class ConfigurationParent {

  private String parentVal;


}
