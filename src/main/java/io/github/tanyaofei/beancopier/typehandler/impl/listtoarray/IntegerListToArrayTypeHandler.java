package io.github.tanyaofei.beancopier.typehandler.impl.listtoarray;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IntegerListToArrayTypeHandler extends AbstractListToArrayTypeHandler<Integer> {
  @Override
  public Integer[] handle(@NotNull List<Integer> value) {
    return value.toArray(new Integer[0]);
  }
}
