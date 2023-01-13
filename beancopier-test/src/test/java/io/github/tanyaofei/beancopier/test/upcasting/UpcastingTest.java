package io.github.tanyaofei.beancopier.test.upcasting;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.test.BeanCopierTest;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author tanyaofei
 */
public class UpcastingTest extends BeanCopierTest {

  @Test
  public void testUpcasting() {
    var source = new UpcastingSource()
        .setValue(new StringValue("1"))
        .setNumber(1)
        .setNumbers(List.of(1, 2, 3));

    var target = BeanCopier.copy(source, UpcastingTarget.class);
    assertEquals(source.getNumber(), target.getNumber());
    assertEquals(source.getNumbers(), target.getNumbers());
  }


}
