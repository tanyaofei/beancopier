package io.github.tanyaofei.beancopier.core.local.impl;

import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.invoker.ExecutableInvoker;
import io.github.tanyaofei.beancopier.core.local.LocalDefiner;
import io.github.tanyaofei.beancopier.core.local.LocalDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalsDefinitionContext;
import io.github.tanyaofei.guava.common.reflect.TypeToken;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Type;

/**
 * A definer for defining a local variable which is compatible.
 * If `fullTypeMatching` is true, it will strictly compare whether two types are equals,
 * otherwise it will compare whether they are compatible.
 * If the judgment indicates that the field can be copied,
 * then the value of field will be retrieved from `Source` and stored in local variable table.
 *
 * @author tanyaofei
 */
public class CompatibleLocalDefiner extends LocalDefiner {

  @Override
  protected boolean defineInternal(
      MethodVisitor v,
      ConverterDefinition converterDefinition,
      LocalDefinition localDefinition,
      LocalsDefinitionContext context
  ) {
    var member = context.getSourceMembers().get(localDefinition.getName());
    if (member == null) {
      return false;
    }

    if (converterDefinition.getConfiguration().isFullTypeMatching()) {
      if (!isFullTypeMatched(member.getGenericType(), localDefinition.getGenericType())) {
        return false;
      }
    } else {
      if (!isTypeCompatible(member.getGenericType(), localDefinition.getGenericType())) {
        return false;
      }
    }

    var getter = ExecutableInvoker.invoker(member.getMethod());
    loadSource(v);
    getter.invoke(v);
    storeLocal(v, localDefinition.getType(), context);
    return true;
  }

  /**
   * @param type1 type 1
   * @param type2 type 2
   * @return true if two types are equals
   */
  protected boolean isFullTypeMatched(Type type1, Type type2) {
    return type1.equals(type2);
  }

  /**
   * @param sourceType type of field from source
   * @param targetType type of field from target
   * @return true if `targetType` is a subtype of source type
   */
  protected boolean isTypeCompatible(Type sourceType, Type targetType) {
    return TypeToken.of(sourceType).isSubtypeOf(targetType);
  }

}
