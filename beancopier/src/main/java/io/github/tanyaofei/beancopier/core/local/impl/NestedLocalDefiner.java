package io.github.tanyaofei.beancopier.core.local.impl;

import io.github.tanyaofei.beancopier.constants.MethodNames;
import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.invoker.ExecutableInvoker;
import io.github.tanyaofei.beancopier.core.local.LocalDefiner;
import io.github.tanyaofei.beancopier.core.local.LocalDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalsDefinitionContext;
import lombok.var;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Type;

/**
 * A definer for define a nested field variable.
 * <pre>{@code
 *  public record Source(Source nested) {}
 *  public record Target(Target nested) {}
 * }</pre>
 * In this case, the field named "nested" is a Nested Field,
 * which means that a recursive bytecode will be generated to complete the local variable definition.
 *
 * <pre>{@code
 *  Target nested = this.convert(source.nested());
 * }
 * </pre>
 *
 * @author tanyaofei
 * @see CollectionNestedLocalDefiner
 */
public class NestedLocalDefiner extends LocalDefiner {

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

    if (!isNested(converterDefinition, member.getGenericType(), localDefinition.getGenericType())) {
      return false;
    }

    loadThis(v);
    loadSource(v);
    var getter = ExecutableInvoker.invoker(member.getMethod());
    getter.invoke(v);
    v.visitMethodInsn(INVOKESPECIAL, converterDefinition.getInternalName(), MethodNames.Converter$convert, converterDefinition.getConvertMethodDescriptor(), false);
    storeLocal(v, localDefinition.getType(), context);
    return true;
  }


  private boolean isNested(ConverterDefinition definition, Type getterReturnType, Type localType) {
    var configuration = definition.getConfiguration();
    if (!configuration.isPreferNested()) {
      return false;
    }

    return definition.getSourceType().equals(getterReturnType) && definition.getTargetType().equals(localType);
  }

}
