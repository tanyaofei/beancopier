package io.github.tanyaofei.beancopier.core.local.impl;

import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.invoker.ExecutableInvoker;
import io.github.tanyaofei.beancopier.core.local.LocalDefiner;
import io.github.tanyaofei.beancopier.core.local.LocalDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalsDefinitionContext;
import io.github.tanyaofei.beancopier.utils.GenericType;
import io.github.tanyaofei.guava.common.reflect.TypeToken;
import org.objectweb.asm.MethodVisitor;

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
      ConverterDefinition converter,
      LocalDefinition local,
      LocalsDefinitionContext context
  ) {
    var provider = context.getProviders().get(local.getName());
    if (provider == null) {
      return false;
    }

    if (converter.getConfiguration().isFullTypeMatching()) {
      if (!isFullTypeMatched(provider.getType(), local.getType())) {
        return false;
      }
    } else {
      if (!isTypeCompatible(provider.getType(), local.getType())) {
        return false;
      }
    }

    var getter = ExecutableInvoker.invoker(provider.getMethod());
    loadSource(v);
    getter.invoke(v);
    storeLocal(v, local.getType().getRawType(), context);
    return true;
  }

  /**
   * @param type1 type 1
   * @param type2 type 2
   * @return true if two types are equals
   */
  protected boolean isFullTypeMatched(GenericType<?> type1, GenericType<?> type2) {
    return type1.equals(type2);
  }

  /**
   * @param providerType type of field from source
   * @param consumerType type of field from target
   * @return true if `consumerType` is a subtype of source type
   */
  protected boolean isTypeCompatible(GenericType<?> providerType, GenericType<?> consumerType) {
    return TypeToken.of(providerType.getGenericType()).isSubtypeOf(consumerType.getGenericType());
  }

}
