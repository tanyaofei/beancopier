package io.github.tanyaofei.beancopier.typehandler.impl.arraytolist;

import java.util.Arrays;
import java.util.List;

public class LongArrayToListTypeHandler extends ArrayToListTypeHandler<Long> {
    @Override
    public List<Long> handle(Long[] value) {
        return value == null ? null : Arrays.asList(value);
    }
}
