package io.github.tanyaofei.beancopier.typehandler.impl.arraytolist;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ShortArrayToListTypeHandler extends AbstractArrayToListTypeHandler<Short> {
  @Override
  public List<Short> handle(Short @NotNull [] value) {
    return Arrays.asList(value);
  }
}
