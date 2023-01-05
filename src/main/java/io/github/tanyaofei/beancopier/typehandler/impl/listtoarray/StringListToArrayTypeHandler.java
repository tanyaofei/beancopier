package io.github.tanyaofei.beancopier.typehandler.impl.listtoarray;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StringListToArrayTypeHandler extends AbstractListToArrayTypeHandler<String> {
  @Override
  public String[] handle(@NotNull List<String> value) {
    return value.toArray(new String[0]);
  }
}
