package io.github.tanyaofei.beancopier.utils;

import io.github.tanyaofei.beancopier.Converter;
import io.github.tanyaofei.beancopier.annotation.Property;
import org.objectweb.asm.Type;

import java.lang.invoke.LambdaMetafactory;
import java.util.List;
import java.util.function.Function;

public interface Constants {

  String SOURCE_FILE = "<generated>";

  Property DEFAULT_PROPERTY = DefaultProperty.DEFAULT_PROPERTY;

  String OBJECT_INTERNAL_NAME = Type.getInternalName(Object.class);

  String CONVERTER_CONVERT_METHOD_DESCRIPTOR = Type.getMethodDescriptor(MethodConstants.CONVERTER$CONVERT);

  String[] CONVERTER_INTERNAL_NAME_ARRAY = {Type.getInternalName(Converter.class)};

  String LIST_INTERNAL_NAME = Type.getInternalName(List.class);

  String LAMBDA_METAFACTORY_INTERNAL_NAME = Type.getInternalName(LambdaMetafactory.class);

  String LAMBDA_META_FACTORY_METAFACTORY_METHOD_DESCRIPTOR = Type.getMethodDescriptor(MethodConstants.LAMBDA_META_FACTORY$METAFACOTRY);

  String FUNCTION_DESCRIPTOR = Type.getDescriptor(Function.class);

  Type CONVERTER_TYPE = Type.getType(CodeEmitter.getMethodDescriptor(Object.class, Object.class));

  ClassInfo OBJECT_CLASS_INFO = ClassInfo.of(Object.class);

  class DefaultProperty {

    static Property DEFAULT_PROPERTY;

    static {
      try {
        DEFAULT_PROPERTY = DefaultProperty.class.getDeclaredField("field").getAnnotation(Property.class);
      } catch (NoSuchFieldException e) {
        throw new IllegalStateException(e);
      }
    }

    @Property
    @SuppressWarnings("unused")
    private byte field;


  }


}
