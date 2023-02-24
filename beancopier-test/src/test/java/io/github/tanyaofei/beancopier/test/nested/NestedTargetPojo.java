package io.github.tanyaofei.beancopier.test.nested;

import io.github.tanyaofei.beancopier.annotation.Property;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author tanyaofei
 */
@Data
@Accessors(chain = true)
public class NestedTargetPojo implements Comparable<NestedTargetPojo> {
  private Integer id;
  private NestedTargetPojo child;

  @Property("children")
  private Collection<NestedTargetPojo> children1;
  @Property("children")
  private Set<NestedTargetPojo> children2;
  @Property("children")
  private HashSet<NestedTargetPojo> children3;
  @Property("children")
  private TreeSet<NestedTargetPojo> children4;
  @Property("children")
  private List<NestedTargetPojo> children5;
  @Property("children")
  private ArrayList<NestedTargetPojo> children6;
  @Property("children")
  private LinkedList<NestedTargetPojo> children7;
  @Property("children")
  private Iterable<NestedTargetPojo> children9;
  @Property("children")
  private NestedTargetPojo[] children8;

  @Property("children2")
  private Collection<NestedTargetPojo> children21;
  @Property("children2")
  private Set<NestedTargetPojo> children22;
  @Property("children2")
  private HashSet<NestedTargetPojo> children23;
  @Property("children2")
  private TreeSet<NestedTargetPojo> children24;
  @Property("children2")
  private List<NestedTargetPojo> children25;
  @Property("children2")
  private ArrayList<NestedTargetPojo> children26;
  @Property("children2")
  private LinkedList<NestedTargetPojo> children27;
  @Property("children2")
  private Iterable<NestedTargetPojo> children29;
  @Property("children2")
  private NestedTargetPojo[] children28;

  @Override
  public int compareTo(@NotNull NestedTargetPojo o) {
    return this.id - o.id;
  }
}
