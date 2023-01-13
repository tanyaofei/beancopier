package io.github.tanyaofei.beancopier.test.callback;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.test.BeanCopierTest;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

/**
 * @author tanyaofei
 */
public class CallbackTest extends BeanCopierTest {

  @Test
  public void testCopyCallback() {
    var source = new CallbackObject().setString("source");
    var target = BeanCopier.copy(source, CallbackObject.class, (s, t) -> t.setString("target"));
    assertEquals("target", target.getString());
  }

  @Test
  public void testCopyListCallback() {
    var sources = IntStream.range(0, 10).mapToObj(ignored -> new CallbackObject().setString("source")).toList();
    var targets = BeanCopier.copyList(sources, CallbackObject.class, (s, t) -> t.setString("target"));
    for (var target : targets) {
      assertEquals("target", target.getString());
    }
  }

}
