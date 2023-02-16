package io.github.tanyaofei.beancopier.test.property.alias;

import io.github.tanyaofei.beancopier.annotation.Alias;
import io.github.tanyaofei.beancopier.annotation.Property;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author tanyaofei
 */
@Data
@Accessors(chain = true)
public class AliasPOJO {

  @Property(alias = {
      @Alias(value = "name2", forType = AliasPOJO2.class),
      @Alias(value = "name3", forType = AliasPOJO3.class)
  })
  private String name;


}
