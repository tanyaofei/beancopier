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

  String INTERNAL_NAME_OBJECT = Type.getInternalName(Object.class);

  String[] INTERNAL_NAME_ARRAY_CONVERTER = {Type.getInternalName(Converter.class)};

  String INTERNAL_NAME_LIST = Type.getInternalName(List.class);

  String INTERNAL_NAME_LAMBDA_METAFACTORY = Type.getInternalName(LambdaMetafactory.class);

  String METHOD_DESCRIPTOR_CONVERTER_CONVERT = Type.getMethodDescriptor(MethodConstants.CONVERTER$CONVERT);

  String METHOD_NAME_LAMBDA$CONVERT$0 = "lambda$convert$0";

  String METHOD_DESCRIPTOR_LAMBDA_META_FACTORY_METAFACTORY = Type.getMethodDescriptor(MethodConstants.LAMBDA_META_FACTORY$METAFACOTRY);

  String TYPE_DESCRIPTOR_FUNCTION = Type.getDescriptor(Function.class);

  Type METHOD_TYPE_CONVERTER = Type.getType(CodeEmitter.getMethodDescriptor(Object.class, Object.class));

  ClassInfo CLASS_INFO_OBJECT = ClassInfo.of(Object.class);

  String INTERNAL_EXCEPTION_MESSAGE = "It should be a bug from beancopier, please submit an issue on github";

  class DefaultProperty {

    static Property DEFAULT_PROPERTY;

    static {
      try {
        DEFAULT_PROPERTY = DefaultProperty.class.getDeclaredField("field").getAnnotation(Property.class);
      } catch (NoSuchFieldException e) {
        throw new IllegalStateException(INTERNAL_EXCEPTION_MESSAGE, e);
      }
    }

    @Property
    @SuppressWarnings("unused")
    private byte field;


  }


}
