package io.github.tanyaofei.beancopier.constants;

import io.github.tanyaofei.beancopier.converter.Converter;
import org.objectweb.asm.Type;

/**
 * @author tanyaofei
 */
public interface InternalNames {

  String Object = Type.getInternalName(Object.class);

  String List = Type.getInternalName(java.util.List.class);

  String LambdaMetafactory = Type.getInternalName(java.lang.invoke.LambdaMetafactory.class);

  String[] AConverter = new String[]{Type.getInternalName(Converter.class)};

}
