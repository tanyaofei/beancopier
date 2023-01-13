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
 * 类型兼容变量定义器
 * @author tanyaofei
 * @since 0.2.0
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

  protected boolean isFullTypeMatched(Type fromType, Type toType) {
    return fromType.equals(toType);
  }

  protected boolean isTypeCompatible(Type fromType, Type toType) {
    return TypeToken.of(fromType).isSubtypeOf(toType);
  }

}
