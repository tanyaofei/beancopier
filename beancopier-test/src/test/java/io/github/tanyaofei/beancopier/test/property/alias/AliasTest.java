package io.github.tanyaofei.beancopier.test.property.alias;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.extenstion.BeanCopierDebugExecution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author tanyaofei
 */
@ExtendWith(BeanCopierDebugExecution.class)
public class AliasTest extends Assertions {

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
