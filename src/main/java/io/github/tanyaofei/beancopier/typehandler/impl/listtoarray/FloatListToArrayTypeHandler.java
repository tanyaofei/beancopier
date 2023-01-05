package io.github.tanyaofei.beancopier.typehandler.impl.listtoarray;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FloatListToArrayTypeHandler extends AbstractListToArrayTypeHandler<Float> {
  @Override
  public Float[] handle(@NotNull List<Float> value) {
    return value.toArray(new Float[0]);
  }
}
