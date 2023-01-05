package io.github.tanyaofei.beancopier.typehandler.impl.listtoarray;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LongListToArrayTypeHandler extends AbstractListToArrayTypeHandler<Long> {
  @Override
  public Long[] handle(@NotNull List<Long> value) {
    return value.toArray(new Long[0]);
  }
}
