package io.github.tanyaofei.beancopier.test.nested;

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
public class NestedTest extends Assertions {

  @Test
  public void testPojo() {
    var source = new NestedSourcePojo()
        .setId(1)
        .setChild(new NestedSourcePojo().setId(2))
        .setChildren(List.of(new NestedSourcePojo().setId(3), new NestedSourcePojo().setId(4)));

    var target = BeanCopier.copy(source, NestedTargetPojo.class);
    assertEquals(source.getId(), target.getId());
    assertEquals(source.getChild().getId(), target.getChild().getId());
    assertEquals(source.getChildren().size(), target.getChildren().size());
  }

  @Test
  public void testRecord() {
    var source = new NestedSourceRecord(
        1,
        new NestedSourceRecord(2, null, null),
        List.of(new NestedSourceRecord(3, null, null), new NestedSourceRecord(4, null, null))
    );

    var target = BeanCopier.copy(source, NestedTargetRecord.class);
    assertEquals(source.id(), target.id());
    assertEquals(source.child().id(), target.child().id());
    assertEquals(source.children().size(), target.children().size());
  }

  @Test
  public void testPojoAndRecord() {
    var source = new NestedSourcePojo()
        .setId(1)
        .setChild(new NestedSourcePojo().setId(2))
        .setChildren(List.of(new NestedSourcePojo().setId(3), new NestedSourcePojo().setId(4)));

    var target = BeanCopier.copy(source, NestedSourceRecord.class);
    assertEquals(source.getId(), target.id());
    assertEquals(source.getChild().getId(), target.child().id());
    assertEquals(source.getChildren().size(), target.children().size());

    var target2 = BeanCopier.copy(target, NestedSourcePojo.class);
    assertEquals(target.id(), target2.getId());
    assertEquals(target.child().id(), target2.getChild().getId());
    assertEquals(target.children().size(), target2.getChildren().size());
  }


}
