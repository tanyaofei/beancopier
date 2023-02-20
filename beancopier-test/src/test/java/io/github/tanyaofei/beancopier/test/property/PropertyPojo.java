package io.github.tanyaofei.beancopier.test.property;

import io.github.tanyaofei.beancopier.annotation.Property;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author tanyaofei
 */
@Data
@Accessors(chain = true)
public class PropertyPojo {

  @Property(value = "name")
  private String id;

  @Property("id")
  private String name;

  @Property(skip = true)
  private int age;

  private String sex;

}
