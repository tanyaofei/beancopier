package io.github.tanyaofei.beancopier.core.local.impl;

import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalsDefinitionContext;
import org.objectweb.asm.MethodVisitor;

import javax.annotation.Nonnull;

public class NoNameMatchedLocalDefiner extends ZeroValueLocalDefiner {

  /**
   * If the source does not have a field with the given name, define a zero value.
   */
  @Override
  public boolean defineInternal(
      @Nonnull MethodVisitor v,
      @Nonnull ConverterDefinition converter,
      @Nonnull LocalDefinition local,
      @Nonnull LocalsDefinitionContext context
  ) {
    if (!context.getProviders().containsKey(local.getName())) {
      return super.defineInternal(v, converter, local, context);
    }
    return false;
  }
}