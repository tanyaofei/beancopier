package io.github.tanyaofei.beancopier.test.upcasting;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.extenstion.DumpConverterClassesExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

/**
 * @author tanyaofei
 */
@ExtendWith(DumpConverterClassesExtension.class)
public class UpcastingTest extends Assertions {

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
