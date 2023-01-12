package io.github.tanyaofei.beancopier;


import io.github.tanyaofei.beancopier.utils.ClassSignature;
import lombok.SneakyThrows;
import org.objectweb.asm.Type;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author tanyaofei
 * @since 0.1.6
 */
interface InternalNames {

  String Object = Type.getInternalName(Object.class);

  String List = Type.getInternalName(java.util.List.class);
  String LambdaMetafactory = Type.getInternalName(java.lang.invoke.LambdaMetafactory.class);
  String[] aConverter = new String[]{Type.getInternalName(Converter.class)};

}

/**
 * @author tanyaofei
 * @since 0.1.6
 */
interface MethodTypes {
  Type Converter$convert = Type.getMethodType(Type.getType(Object.class), Type.getType(Object.class));
}

/**
 * @author tanyaofei
 * @since 0.1.6
 */
interface MethodInvokers {

  MethodInvoker Stream$map = MethodInvoker.methodInvoker(Methods.Stream$map());

  MethodInvoker Stream$collect = MethodInvoker.methodInvoker(Methods.Stream$collect());

  MethodInvoker List$stream = MethodInvoker.methodInvoker(Methods.List$stream());

  MethodInvoker Collector$toList = MethodInvoker.methodInvoker(Methods.Collectors$toList());

}

/**
 * @author tanyaofei
 * @since 0.1.6
 */
interface MethodNames {

  String lambda$convert$0 = "lambda$convert$0";

  String Object$init = "<init>";

  String Lambda$MetaFactory$metafactory = "metafactory";

  String Function$apply = "apply";

  String Converter$convert = "convert";

}

/**
 * @author tanyaofei
 * @since 0.1.6
 */
interface Methods {

  @SneakyThrows
  static Method Converter$convert() {
    return Converter.class.getMethod("convert", Object.class);
  }

  @SneakyThrows
  static Method List$stream() {
    return List.class.getMethod("stream");
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


/**
 * @author tanyaofei
 * @since 0.1.6
 */
interface MethodDescriptors {

  String LambdaMetafactory$metafactory = Type.getMethodDescriptor(Methods.lambdaMetaFactory$metafacotry());

  String Converter$convert = Type.getMethodDescriptor(Methods.Converter$convert());

  String Object$init = "()V";

}


/**
 * @author tanyaofei
 * @since 0.1.6
 */
interface TypeDescriptors {
  String Function = Type.getDescriptor(Function.class);

}

/**
 * @author tanyaofei
 * @since 0.1.6
 */
interface ClassInfos {

  ClassSignature.ClassInfo Object = ClassSignature.ClassInfo.of(Object.class);

}