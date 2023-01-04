package io.github.tanyaofei.beancopier.typehandler.impl.arraytolist;

import java.util.Arrays;
import java.util.List;

public class ShortArrayToListTypeHandler extends ArrayToListTypeHandler<Short> {
    @Override
    public List<Short> handle(Short[] value) {
        return value == null ? null : Arrays.asList(value);
    }
}
