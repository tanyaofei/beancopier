package io.github.tanyaofei.beancopier.typehandler.impl.listtoarray;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShortListToArrayTypeHandler extends AbstractListToArrayTypeHandler<Short> {
  @Override
  public Short[] handle(@NotNull List<Short> value) {
    return value.toArray(new Short[0]);
  }
}
