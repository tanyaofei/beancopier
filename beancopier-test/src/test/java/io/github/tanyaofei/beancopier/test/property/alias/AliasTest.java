package io.github.tanyaofei.beancopier.test.property.alias;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.test.BeanCopierTest;
import org.junit.jupiter.api.Test;

/**
 * @author tanyaofei
 */
public class AliasTest extends BeanCopierTest {

  @Test
  public void testAliasForPOJO() {
    var fromPOPJO2 = BeanCopier.copy(new AliasPOJO2().setName2("name"), AliasPOJO.class);
    assertEquals("name", fromPOPJO2.getName());

    var fromPOPJO3 = BeanCopier.copy(new AliasPOJO3().setName3("name"), AliasPOJO.class);
    assertEquals("name", fromPOPJO3.getName());
  }

  @Test
  public void testAliasForRecord() {
    var fromPOPJO2 = BeanCopier.copy(new AliasPOJO2().setName2("name"), AliasRecord.class);
    assertEquals("name", fromPOPJO2.name());

    var fromPOPJO3 = BeanCopier.copy(new AliasPOJO3().setName3("name"), AliasRecord.class);
    assertEquals("name", fromPOPJO3.name());
  }

}
