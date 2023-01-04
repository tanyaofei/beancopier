package io.github.tanyaofei.beancopier.typehandler.impl.arraytolist;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class LongArrayToListTypeHandler extends AbstractArrayToListTypeHandler<Long> {
    @Override
    public List<Long> handle(Long @NotNull [] value) {
        return Arrays.asList(value);
    }
}
