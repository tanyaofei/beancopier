package io.github.tanyaofei.beancopier.test.configuration;

import io.github.tanyaofei.beancopier.annotation.Property;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author tanyaofei
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ConfigurationPojo extends ConfigurationParentPojo {

  private String stringVal = "stringVal";

  private int intVal = 1;

  private List<ConfigurationPojo> nestedList = List.of();

  private ConfigurationPojo nested = null;

  private boolean booleanVal = true;

  private Integer integerVal = 1;

  @Property(skip = true)
  private String skippedVal;

}
