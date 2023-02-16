package io.github.tanyaofei.beancopier.test.upcasting;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.test.BeanCopierTest;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * @author tanyaofei
 */
public class UpcastingTest extends BeanCopierTest {

  @Test
  public void testUpcasting() {
    var source = new UpcastingSource()
        .setValue(new StringValue("1"))
        .setNumber(1)
        .setNumbers(Arrays.asList(1, 2, 3));

    var target = BeanCopier.copy(source, UpcastingTarget.class);
    assertEquals(source.getNumber(), target.getNumber());
    assertEquals(source.getNumbers(), target.getNumbers());
  }


}
