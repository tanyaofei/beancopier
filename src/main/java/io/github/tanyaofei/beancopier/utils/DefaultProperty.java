package io.github.tanyaofei.beancopier.utils;

import io.github.tanyaofei.beancopier.annotation.Property;

public class DefaultProperty {

    @Property
    @SuppressWarnings("unused")
    private byte field;

    public static Property DEFAULT_PROPERTY;

    static {
        try {
            DEFAULT_PROPERTY = DefaultProperty.class.getDeclaredField("field").getAnnotation(Property.class);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }


}
