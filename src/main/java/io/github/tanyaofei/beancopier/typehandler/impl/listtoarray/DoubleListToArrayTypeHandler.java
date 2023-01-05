package io.github.tanyaofei.beancopier.typehandler.impl.listtoarray;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DoubleListToArrayTypeHandler extends AbstractListToArrayTypeHandler<Double> {
  @Override
  public Double[] handle(@NotNull List<Double> value) {
    return value.toArray(new Double[0]);
  }
}
