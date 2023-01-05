package io.github.tanyaofei.beancopier.typehandler.impl.arraytolist;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class IntegerArrayToListTypeHandler extends AbstractArrayToListTypeHandler<Integer> {
  @Override
  public List<Integer> handle(Integer @NotNull [] value) {
    return Arrays.asList(value);
  }
}
