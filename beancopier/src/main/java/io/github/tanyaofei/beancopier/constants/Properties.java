package io.github.tanyaofei.beancopier.constants;

import io.github.tanyaofei.beancopier.annotation.Property;
import io.github.tanyaofei.beancopier.utils.reflection.member.BeanMember;
import lombok.SneakyThrows;

import java.util.WeakHashMap;

/**
 * @author tanyaofei
 */
public interface Properties {

  WeakHashMap<Object, Property> propertiesCache = new WeakHashMap<>(32);

  Property defaultProperty = defaultProperty();

  @SneakyThrows
  private static Property defaultProperty() {
    return DefaultPropertyHolder.class.getField("b").getAnnotation(Property.class);
  }

  class DefaultPropertyHolder {

    @Property
    public byte b;

  }

  static Property getOrDefault(BeanMember member) {
    var cacheKey = member.getIdentify();
    var property = propertiesCache.get(cacheKey);
    if (property == null) {
      property = member.getAnnotation(Property.class);
      if (property == null) {
        property = defaultProperty;
      }
      synchronized (propertiesCache) {
        propertiesCache.put(cacheKey, property);
      }
    }
    return property;
  }


}
