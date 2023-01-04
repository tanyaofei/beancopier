package io.github.tanyaofei.beancopier.typehandler.impl.listtoarray;

import java.util.List;

public class LongListToArrayTypeHandler extends ListToArrayTypeHandler<Long> {
    @Override
    public Long[] handle(List<Long> value) {
        return value == null ? null : value.toArray(new Long[0]);
    }
}
