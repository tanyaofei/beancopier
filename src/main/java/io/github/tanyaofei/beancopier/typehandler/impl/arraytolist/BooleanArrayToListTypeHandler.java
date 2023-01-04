package io.github.tanyaofei.beancopier.typehandler.impl.arraytolist;

import java.util.Arrays;
import java.util.List;

public class BooleanArrayToListTypeHandler extends ArrayToListTypeHandler<Boolean> {
    @Override
    public List<Boolean> handle(Boolean[] value) {
        return value == null ? null : Arrays.asList(value);
    }
}
