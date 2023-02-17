package io.github.tanyaofei.beancopier.constants;

import io.github.tanyaofei.beancopier.converter.AbstractConverter;
import io.github.tanyaofei.beancopier.converter.Converter;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author tanyaofei
 */
public interface Methods {

  @SneakyThrows
  static Method Converter$convert() {
    return Converter.class.getMethod("convert", Object.class);
  }

  @SneakyThrows
  static Method AbstractConverter$convertAll() {
    return AbstractConverter.class.getMethod("convertAll", Collection.class);
  }

}
