package io.github.tanyaofei.beancopier.utils;

import io.github.tanyaofei.beancopier.annotation.Property;

public interface Constants {

  String SOURCE_FILE = "<generated>";

  Property DEFAULT_PROPERTY = DefaultProperty.DEFAULT_PROPERTY;

  class DefaultProperty {

    static Property DEFAULT_PROPERTY;

    static {
      try {
        DEFAULT_PROPERTY = io.github.tanyaofei.beancopier.utils.DefaultProperty.class.getDeclaredField("field").getAnnotation(Property.class);
      } catch (NoSuchFieldException e) {
        throw new IllegalStateException(e);
      }
    }

    @Property
    @SuppressWarnings("unused")
    private byte field;


  }


}
