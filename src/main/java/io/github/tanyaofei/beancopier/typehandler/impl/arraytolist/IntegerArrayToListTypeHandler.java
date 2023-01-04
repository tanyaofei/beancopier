package io.github.tanyaofei.beancopier.typehandler.impl.arraytolist;

import java.util.Arrays;
import java.util.List;

public class IntegerArrayToListTypeHandler extends ArrayToListTypeHandler<Integer> {
    @Override
    public List<Integer> handle(Integer[] value) {
        return value == null ? null : Arrays.asList(value);
    }
}
