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

  Method CONVERTER$CONVERT = converter$convert();
  Method LIST$STREAM = list$stream();
  Method STREAM$MAP = stream$map();
  Method STREAM$COLLECT = stream$collect();
  Method COLLECTORS$TO_LIST = collectors$toList();
  Method FUNCTION$APPLY = function$apply();
  Method LAMBDA_META_FACTORY$METAFACOTRY = lambdaMetaFactory$metafacotry();

  Method BOOLEAN$BOOLEAN_VALUE = boolean$booleanValue();
  Method NUMBER$INT_VALUE = number$intValue();
  Method NUMBER$SHORT_VALUE = number$shortValue();
  Method NUMBER$LONG_VALUE = number$longValue();
  Method NUMBER$FLOAT_VALUE = number$floatValue();
  Method NUMBER$DOUBLE_VALUE = number$doubleValue();
  Method BYTE$BYTE_VALUE = byte$byteValue();
  Method CHARACTER$CHAR_VALUE = character$charValue();

  Method BOOLEAN$VALUE_OF = boolean$valueOf();
  Method INTEGER$VALUE_OF = integer$valueOf();
  Method SHORT$VALUE_OF = short$valueOf();
  Method LONG$VALUE_OF = long$valueOf();
  Method FLOAT$VALUE_OF = float$valueOf();
  Method DOUBLE$VALUE_OF = double$valueOf();
  Method CHARACTER$VALUE_OF = character$valueOf();
  Method BYTE$VALUE_OF = byte$valueOf();

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

  @SneakyThrows
  static Method number$intValue() {
    return Number.class.getMethod("intValue");
  }

  @SneakyThrows
  static Method number$shortValue() {
    return Number.class.getMethod("shortValue");
  }

  @SneakyThrows
  static Method number$longValue() {
    return Number.class.getMethod("longValue");
  }

  @SneakyThrows
  static Method number$floatValue() {
    return Number.class.getMethod("floatValue");
  }

  @SneakyThrows
  static Method number$doubleValue() {
    return Number.class.getMethod("doubleValue");
  }

  @SneakyThrows
  static Method byte$byteValue() {
    return Byte.class.getMethod("byteValue");
  }

  @SneakyThrows
  static Method boolean$booleanValue() {
    return Boolean.class.getMethod("booleanValue");
  }

  @SneakyThrows
  static Method character$charValue() {
    return Character.class.getMethod("charValue");
  }

  @SneakyThrows
  static Method boolean$valueOf() {
    return Boolean.class.getMethod("valueOf", boolean.class);
  }

  @SneakyThrows
  static Method integer$valueOf() {
    return Integer.class.getMethod("valueOf", int.class);
  }

  @SneakyThrows
  static Method short$valueOf() {
    return Short.class.getMethod("valueOf", short.class);
  }

  @SneakyThrows
  static Method long$valueOf() {
    return Long.class.getMethod("valueOf", long.class);
  }

  @SneakyThrows
  static Method float$valueOf() {
    return Float.class.getMethod("valueOf", float.class);
  }

  @SneakyThrows
  static Method double$valueOf() {
    return Double.class.getMethod("valueOf", double.class);
  }

  @SneakyThrows
  static Method character$valueOf() {
    return Character.class.getMethod("valueOf", char.class);
  }

  @SneakyThrows
  static Method byte$valueOf() {
    return Byte.class.getMethod("valueOf", byte.class);
  }

}
