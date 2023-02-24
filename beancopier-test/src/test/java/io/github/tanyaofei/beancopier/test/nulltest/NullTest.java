package io.github.tanyaofei.beancopier.test.nulltest;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.extenstion.BeanCopierDebugExecution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author tanyaofei
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@ExtendWith(BeanCopierDebugExecution.class)
public class NullTest extends Assertions {

  public static List<Object> threeNulls = new ArrayList<>() {{
    add(null);
    add(null);
    add(null);
  }};

  public static List<Object> threeNullsAndTwoNonnulls = new ArrayList<>() {{
    add(null);
    add(new Object());
    add(null);
    add(new Object());
    add(null);
  }};

  @Test
  public void testCopy() {
    var afterCopyWasExecuted = new AtomicBoolean();
    BeanCopier.copy(null, Object.class, (s, t) -> afterCopyWasExecuted.set(true));
    assertTrue(afterCopyWasExecuted.get());
  }

  @Test
  public void testCopyList() {
    var count = new AtomicInteger();
    BeanCopier.copyList(threeNulls, Object.class, (s, t) -> count.incrementAndGet());
    assertEquals(3, count.get());

    count.set(0);
    BeanCopier.copyList(threeNullsAndTwoNonnulls, Object.class, (s, t) -> count.incrementAndGet());
    assertEquals(5, count.get());
  }

  @Test
  public void testClone() {
    var afterCloneWasExecuted = new AtomicBoolean();
    BeanCopier.clone(null, (s, t) -> afterCloneWasExecuted.set(true));
    assertTrue(afterCloneWasExecuted.get());
  }

  @Test
  public void testCloneList() {
    var count = new AtomicInteger();
    BeanCopier.cloneList(threeNulls, (s, t) -> count.incrementAndGet());
    assertEquals(3, count.get());

    count.set(0);
    BeanCopier.cloneList(threeNullsAndTwoNonnulls, (s, t) -> count.incrementAndGet());
    assertEquals(5, count.get());
  }
}
