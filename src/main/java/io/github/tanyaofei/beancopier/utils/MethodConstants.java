package io.github.tanyaofei.beancopier.utils;

import io.github.tanyaofei.beancopier.Converter;
import lombok.SneakyThrows;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author tanyaofei
 * @since 0.0.1
 */
public interface MethodConstants {

  Method CONVERTER$CONVERT = converter$convert();
  Method LIST$STREAM = list$stream();
  Method STREAM$MAP = stream$map();
  Method STREAM$COLLECT = stream$collect();
  Method COLLECTORS$TO_LIST = collectors$toList();
  Method FUNCTION$APPLY = function$apply();
  Method LAMBDA_META_FACTORY$METAFACOTRY = lambdaMetaFactory$metafacotry();

  @SneakyThrows
  static Method converter$convert() {
    return Converter.class.getMethod("convert", Object.class);
  }

  @SneakyThrows
  static Method list$stream() {
    return List.class.getMethod("stream");
  }

  @SneakyThrows
  static Method stream$map() {
    return Stream.class.getMethod("map", Function.class);
  }

  @SneakyThrows
  static Method stream$collect() {
    return Stream.class.getMethod("collect", Collector.class);
  }

  @SneakyThrows
  static Method collectors$toList() {
    return Collectors.class.getMethod("toList");
  }

  @SneakyThrows
  static Method function$apply() {
    return Function.class.getMethod("apply", Object.class);
  }

  @SneakyThrows
  static Method lambdaMetaFactory$metafacotry() {
    return LambdaMetafactory.class.getMethod("metafactory",
        Lookup.class,
        String.class,
        MethodType.class,
        MethodType.class,
        MethodHandle.class,
        MethodType.class);
  }

}
