package io.github.tanyaofei.beancopier.typehandler.impl.listtoarray;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BooleanListToArrayTypeHandler extends AbstractListToArrayTypeHandler<Boolean> {
    @Override
    public Boolean[] handle(@NotNull List<Boolean> value) {
        return value.toArray(new Boolean[0]);
    }
}
