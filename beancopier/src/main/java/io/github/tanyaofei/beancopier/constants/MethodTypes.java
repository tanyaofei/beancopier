package io.github.tanyaofei.beancopier.constants;

import org.objectweb.asm.Type;

/**
 * @author tanyaofei
 */
public interface MethodTypes {

  Type Converter$convert = Type.getMethodType(Type.getType(Object.class), Type.getType(Object.class));

}
