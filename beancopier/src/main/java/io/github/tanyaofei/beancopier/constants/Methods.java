package io.github.tanyaofei.beancopier.constants;

import io.github.tanyaofei.beancopier.converter.Converter;
import lombok.SneakyThrows;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
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
  static Method Collection$stream() {
    return Collection.class.getMethod("stream");
  }

  @SneakyThrows
  static Method Stream$map() {
    return Stream.class.getMethod("map", Function.class);
  }

  @SneakyThrows
  static Method Stream$collect() {
    return Stream.class.getMethod("collect", Collector.class);
  }

  @SneakyThrows
  static Method Collectors$toList() {
    return Collectors.class.getMethod("toList");
  }

  @SneakyThrows
  static Method lambdaMetaFactory$metafacotry() {
    return LambdaMetafactory.class.getMethod("metafactory",
        MethodHandles.Lookup.class,
        String.class,
        MethodType.class,
        MethodType.class,
        MethodHandle.class,
        MethodType.class);
  }

}
