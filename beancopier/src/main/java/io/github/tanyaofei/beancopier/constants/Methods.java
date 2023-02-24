package io.github.tanyaofei.beancopier.constants;

import io.github.tanyaofei.beancopier.converter.AbstractConverter;
import io.github.tanyaofei.beancopier.converter.Converter;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.stream.Stream;

/**
 * @author tanyaofei
 */
public interface Methods {

  @SneakyThrows
  static Method Converter$convert() {
    return Converter.class.getMethod("convert", Object.class);
  }

  @SneakyThrows
  static Method AbstractConverter$convertIterableToStream() {
    return AbstractConverter.class.getDeclaredMethod("convertIterableToStream", Iterable.class);
  }

  @SneakyThrows
  static Method AbstractConverter$convertArrayToStream() {
    return AbstractConverter.class.getDeclaredMethod("convertArrayToStream", Object[].class);
  }

  @SneakyThrows
  static Method AbstractConverter$collectToSet() {
    return AbstractConverter.class.getDeclaredMethod("collectToSet", Stream.class);
  }

  @SneakyThrows
  static Method AbstractConverter$collectToHashSet() {
    return AbstractConverter.class.getDeclaredMethod("collectToHashSet", Stream.class);
  }

  @SneakyThrows
  static Method AbstractConverter$collectToTreeSet() {
    return AbstractConverter.class.getDeclaredMethod("collectToTreeSet", Stream.class);
  }

  @SneakyThrows
  static Method AbstractConverter$collectToList() {
    return AbstractConverter.class.getDeclaredMethod("collectToList", Stream.class);
  }

  @SneakyThrows
  static Method AbstractConverter$collectToArrayList() {
    return AbstractConverter.class.getDeclaredMethod("collectToArrayList", Stream.class);
  }

  @SneakyThrows
  static Method AbstractConverter$collectToLinkedList() {
    return AbstractConverter.class.getDeclaredMethod("collectToLinkedList", Stream.class);
  }

  @SneakyThrows
  static Method AbstractConverter$collectToArray() {
    return AbstractConverter.class.getDeclaredMethod("collectToArray", Stream.class);
  }

}
