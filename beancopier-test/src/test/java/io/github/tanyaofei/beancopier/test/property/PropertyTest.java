package io.github.tanyaofei.beancopier.test.property;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.test.BeanCopierTest;
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

  @Test
  public void testPropertyRecord() {
    var source = new PropertyRecord("id", "name", 18, "male");
    var target = BeanCopier.clone(source);
    assertEquals(source.id(), target.id());
    assertEquals(source.name(), target.name());
    assertEquals(0, target.age());
    assertEquals(source.sex(), target.sex());
  }

}
