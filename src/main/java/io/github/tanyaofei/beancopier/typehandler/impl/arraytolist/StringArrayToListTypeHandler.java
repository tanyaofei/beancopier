package io.github.tanyaofei.beancopier.typehandler.impl.arraytolist;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class StringArrayToListTypeHandler extends AbstractArrayToListTypeHandler<String> {
    @Override
    public List<String> handle(String @NotNull [] value) {
        return Arrays.asList(value);
    }
}
