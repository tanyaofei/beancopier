package io.github.tanyaofei.beancopier.constants;

import io.github.tanyaofei.beancopier.converter.AbstractConverter;
import io.github.tanyaofei.beancopier.converter.Converter;
import lombok.SneakyThrows;

import java.lang.reflect.Method;

/**
 * @author tanyaofei
 */
public interface Methods {

  @SneakyThrows
  static Method Converter$convert() {
    return Converter.class.getMethod("convert", Object.class);
  }

  @SneakyThrows
  static Method AbstractConverter$convertAllToList() {
    return AbstractConverter.class.getMethod("convertAllToList", Iterable.class);
  }

  @SneakyThrows
  static Method AbstractConverter$convertAllToSet() {
    return AbstractConverter.class.getMethod("convertAllToSet", Iterable.class);
  }

  @SneakyThrows
  static Method AbstractConverter$convertAllToArrayList() {
    return AbstractConverter.class.getMethod("convertAllToArrayList", Iterable.class);
  }

  @SneakyThrows
  static Method AbstractConverter$convertAllToLinkedList() {
    return AbstractConverter.class.getMethod("convertAllToLinkedList", Iterable.class);
  }


}
