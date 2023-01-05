package io.github.tanyaofei.beancopier.typehandler.impl.listtoarray;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ByteListToArrayTypeHandler extends AbstractListToArrayTypeHandler<Byte> {
  @Override
  public Byte[] handle(@NotNull List<Byte> value) {
    return value.toArray(new Byte[0]);
  }
}
