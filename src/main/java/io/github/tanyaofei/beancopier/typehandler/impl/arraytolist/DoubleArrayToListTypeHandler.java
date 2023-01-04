package io.github.tanyaofei.beancopier.typehandler.impl.arraytolist;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class DoubleArrayToListTypeHandler extends AbstractArrayToListTypeHandler<Double> {
    @Override
    public List<Double> handle(Double @NotNull [] value) {
        return Arrays.asList(value);
    }
}
