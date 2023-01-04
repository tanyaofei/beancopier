package io.github.tanyaofei.beancopier.typehandler.impl.arraytolist;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ByteArrayToListTypeHandler extends AbstractArrayToListTypeHandler<Byte> {
    @Override
    public List<Byte> handle(Byte @NotNull [] value) {
        return Arrays.asList(value);
    }
}
