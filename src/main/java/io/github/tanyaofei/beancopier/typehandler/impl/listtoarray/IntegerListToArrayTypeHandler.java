package io.github.tanyaofei.beancopier.typehandler.impl.listtoarray;

import java.util.List;

public class IntegerListToArrayTypeHandler extends ListToArrayTypeHandler<Integer> {
    @Override
    public Integer[] handle(List<Integer> value) {
        return value == null ? null : value.toArray(new Integer[0]);
    }
}
