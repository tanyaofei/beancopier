package io.github.tanyaofei.beancopier.utils;

import java.util.AbstractList;

/**
 * @author tanyaofei
 */
public class RefArrayList<E> extends AbstractList<E> {

  private final E[] elements;

  private RefArrayList(E[] elements) {
    this.elements = elements;
  }

  @SafeVarargs
  public static <E> RefArrayList<E> of(E... array) {
    return new RefArrayList<>(array);
  }

  @Override
  public E get(int index) {
    if (index < 0 || index > elements.length) {
      throw new IndexOutOfBoundsException(index);
    }
    return elements[index];
  }

  @Override
  public int size() {
    return elements.length;
  }

}
