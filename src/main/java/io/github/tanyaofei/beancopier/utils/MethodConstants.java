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
 */
public interface MethodConstants {

  Method CONVERTER$CONVERT = converter$ConvertMethod();
  Method LIST$STREAM = list$StreamMethod();
  Method STREAM$MAP = stream$MapMethod();
  Method STREAM$COLLECT = stream$CollectMethod();
  Method COLLECTORS$TO_LIST = collectors$ToListMethod();
  Method FUNCTION$APPLY = function$ApplyMethod();
  Method LAMBDA_META_FACTORY$METAFACOTRY = lambdaMetaFactory$metafacotryMethod();


  @SneakyThrows
  static Method converter$ConvertMethod() {
    return Converter.class.getMethod("convert", Object.class);
  }

  @SneakyThrows
  static Method list$StreamMethod() {
    return List.class.getMethod("stream");
  }

  @SneakyThrows
  static Method stream$MapMethod() {
    return Stream.class.getMethod("map", Function.class);
  }

  @SneakyThrows
  static Method stream$CollectMethod() {
    return Stream.class.getMethod("collect", Collector.class);
  }

  @SneakyThrows
  static Method collectors$ToListMethod() {
    return Collectors.class.getMethod("toList");
  }

  @SneakyThrows
  static Method function$ApplyMethod() {
    return Function.class.getMethod("apply", Object.class);
  }

  @SneakyThrows
  static Method lambdaMetaFactory$metafacotryMethod() {
    return LambdaMetafactory.class.getMethod("metafactory",
        Lookup.class,
        String.class,
        MethodType.class,
        MethodType.class,
        MethodHandle.class,
        MethodType.class);
  }

}
