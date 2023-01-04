package io.github.tanyaofei.beancopier.typehandler.impl.listtoarray;

import java.util.List;

public class StringListToArrayTypeHandler extends ListToArrayTypeHandler<String> {
    @Override
    public String[] handle(List<String> value) {
        return value == null ? null : value.toArray(new String[0]);
    }
}
