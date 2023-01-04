package io.github.tanyaofei.beancopier.typehandler.impl.listtoarray;

import java.util.List;

public class ShortListToArrayTypeHandler extends ListToArrayTypeHandler<Short> {
    @Override
    public Short[] handle(List<Short> value) {
        return value == null ? null : value.toArray(new Short[0]);
    }
}
