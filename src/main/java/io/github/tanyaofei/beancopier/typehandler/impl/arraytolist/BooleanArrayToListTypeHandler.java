package io.github.tanyaofei.beancopier.typehandler.impl.arraytolist;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class BooleanArrayToListTypeHandler extends AbstractArrayToListTypeHandler<Boolean> {
    @Override
    public List<Boolean> handle(Boolean @NotNull [] value) {
        return Arrays.asList(value);
    }
}
