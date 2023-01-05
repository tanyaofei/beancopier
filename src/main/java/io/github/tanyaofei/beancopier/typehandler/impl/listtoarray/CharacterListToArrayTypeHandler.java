package io.github.tanyaofei.beancopier.typehandler.impl.listtoarray;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CharacterListToArrayTypeHandler extends AbstractListToArrayTypeHandler<Character> {
  @Override
  public Character[] handle(@NotNull List<Character> value) {
    return value.toArray(new Character[0]);
  }
}
