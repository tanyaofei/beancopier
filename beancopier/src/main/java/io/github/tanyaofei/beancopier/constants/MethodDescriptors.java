package io.github.tanyaofei.beancopier.constants;

import org.objectweb.asm.Type;

/**
 * @author tanyaofei
 */
public interface MethodDescriptors {

  String LambdaMetafactory$metafactory = Type.getMethodDescriptor(Methods.lambdaMetaFactory$metafacotry());

  String Converter$convert = Type.getMethodDescriptor(Methods.Converter$convert());

  String Object$init = "()V";

}
