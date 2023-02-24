package io.github.tanyaofei.beancopier.test.callback;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.extenstion.BeanCopierDebugExecution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.stream.IntStream;

/**
 * @author tanyaofei
 */
@ExtendWith(BeanCopierDebugExecution.class)
public class CallbackTest extends Assertions {

  @Test
  public void testCopyCallback() {
    var source = new CallbackPojo().setString("source");
    var target = BeanCopier.copy(source, CallbackPojo.class, (s, t) -> t.setString("target"));
    assertEquals("target", target.getString());
  }

  @Test
  public void testCopyListCallback() {
    var sources = IntStream.range(0, 10).mapToObj(ignored -> new CallbackPojo().setString("source")).toList();
    var targets = BeanCopier.copyList(sources, CallbackPojo.class, (s, t) -> t.setString("target"));
    for (var target : targets) {
      assertEquals("target", target.getString());
    }
  }

}
