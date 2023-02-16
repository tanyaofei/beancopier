package io.github.tanyaofei.beancopier.test.property;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.test.BeanCopierTest;
import lombok.var;
import org.junit.jupiter.api.Test;

/**
 * @author tanyaofei
 */
public class PropertyTest extends BeanCopierTest {

  @Test
  public void testPropertyObject() {
    var source = new PropertyPOJO()
        .setId("id")
        .setName("name")
        .setAge(18)
        .setSex("male");

    var target = BeanCopier.clone(source);
    assertEquals(source.getId(), target.getId());
    assertEquals(source.getName(), target.getName());
    assertEquals(0, target.getAge());
    assertEquals(source.getSex(), target.getSex());
  }

}
