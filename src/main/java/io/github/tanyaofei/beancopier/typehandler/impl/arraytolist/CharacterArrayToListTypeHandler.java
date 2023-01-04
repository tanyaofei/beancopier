package io.github.tanyaofei.beancopier.typehandler.impl.arraytolist;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class CharacterArrayToListTypeHandler extends AbstractArrayToListTypeHandler<Character> {
    @Override
    public List<Character> handle(Character @NotNull [] value) {
        return Arrays.asList(value);
    }
}
