package io.github.tanyaofei.beancopier.core.local.impl;

import io.github.tanyaofei.beancopier.constants.MethodNames;
import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.invoker.ExecutableInvoker;
import io.github.tanyaofei.beancopier.core.local.LocalDefiner;
import io.github.tanyaofei.beancopier.core.local.LocalDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalsDefinitionContext;
import io.github.tanyaofei.beancopier.utils.GenericType;
import org.objectweb.asm.MethodVisitor;

import javax.annotation.Nonnull;

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
 * @see IterableNestedLocalDefiner
 */
public class NestedLocalDefiner extends LocalDefiner {

  @Override
  protected boolean defineInternal(
      @Nonnull MethodVisitor v,
      @Nonnull ConverterDefinition converter,
      @Nonnull LocalDefinition local,
      @Nonnull LocalsDefinitionContext context
  ) {
    var provider = context.getProviders().get(local.getName());
    if (provider == null) {
      return false;
    }

    if (!converter.getFeatures().isPreferNested() || !isNested(converter, provider.getType(), local.getType())) {
      return false;
    }

    loadThis(v);
    loadSource(v);
    var getter = ExecutableInvoker.invoker(provider.getMethod());
    getter.invoke(v);
    v.visitMethodInsn(
        INVOKESPECIAL,
        converter.getInternalName(),
        MethodNames.Converter$convert,
        converter.getConvertMethodDescriptor(),
        false
    );
    storeLocal(v, local.getType().getRawType(), context);
    return true;
  }


  private boolean isNested(
      @Nonnull ConverterDefinition definition,
      @Nonnull GenericType<?> providerType,
      @Nonnull GenericType<?> consumerType
  ) {
    var sc = definition.getSourceType();
    var tc = definition.getTargetType();
    return providerType.getGenericType() == sc && consumerType.getGenericType() == tc;
  }

}
