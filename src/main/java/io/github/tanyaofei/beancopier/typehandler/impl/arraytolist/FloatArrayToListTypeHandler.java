package io.github.tanyaofei.beancopier.typehandler.impl.arraytolist;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class FloatArrayToListTypeHandler extends AbstractArrayToListTypeHandler<Float> {
  @Override
  public List<Float> handle(Float @NotNull [] value) {
    return Arrays.asList(value);
  }
}
