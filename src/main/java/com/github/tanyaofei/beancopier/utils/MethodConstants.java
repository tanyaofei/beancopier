package com.github.tanyaofei.beancopier.utils;

import com.github.tanyaofei.beancopier.Converter;
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
 * @since 2021.08.0
 */
public interface MethodConstants {

  Method CONVERTER_CONVERT = converterConvertMethod();
  Method LIST_STREAM = listStreamMethod();
  Method STREAM_MAP = streamMapMethod();
  Method STREAM_COLLECT = streamCollectMethod();
  Method COLLECTORS_TO_LIST = collectorsToListMethod();
  Method FUNCTION_APPLY = functionApplyMethod();
  Method LAMBDA_META_FACTORY_METAFACOTRY = lambdaMetaFactoryLambdafacotryMethod();


  @SneakyThrows
  private static Method converterConvertMethod() {
    return Converter.class.getMethod("convert", Object.class);
  }

  @SneakyThrows
  private static Method listStreamMethod() {
    return List.class.getMethod("stream");
  }

  @SneakyThrows
  private static Method streamMapMethod() {
    return Stream.class.getMethod("map", Function.class);
  }

  @SneakyThrows
  private static Method streamCollectMethod() {
    return Stream.class.getMethod("collect", Collector.class);
  }

  @SneakyThrows
  private static Method collectorsToListMethod() {
    return Collectors.class.getMethod("toList");
  }

  @SneakyThrows
  private static Method functionApplyMethod() {
    return Function.class.getMethod("apply", Object.class);
  }

  @SneakyThrows
  private static Method lambdaMetaFactoryLambdafacotryMethod() {
    return LambdaMetafactory.class.getMethod("metafactory",
        Lookup.class,
        String.class,
        MethodType.class,
        MethodType.class,
        MethodHandle.class,
        MethodType.class);
  }

}
