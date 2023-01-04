package io.github.tanyaofei.beancopier.typehandler.impl.listtoarray;

import java.util.List;

public class BooleanListToArrayTypeHandler extends ListToArrayTypeHandler<Boolean> {
    @Override
    public Boolean[] handle(List<Boolean> value) {
        return value == null ? null : value.toArray(new Boolean[0]);
    }
}
