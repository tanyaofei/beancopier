package io.github.tanyaofei.beancopier.typehandler.impl.arraytolist;

import java.util.Arrays;
import java.util.List;

public class StringArrayToListTypeHandler extends ArrayToListTypeHandler<String> {
    @Override
    public List<String> handle(String[] value) {
        return value == null ? null : Arrays.asList(value);
    }
}
