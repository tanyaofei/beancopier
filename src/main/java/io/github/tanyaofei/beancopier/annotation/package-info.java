package io.github.tanyaofei.beancopier.annotation;


/**
 * @author tanyaofei
 * @since 0.1.6
 */
class PropertyProxy {
  @Property
  public String s;

  public static Property DEFAULT;

  static {
    try {
      DEFAULT = PropertyProxy.class.getField("s").getAnnotation(Property.class);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }

}

