package io.github.tanyaofei.beancopier.core.local.impl;

import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalsDefinitionContext;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.MethodVisitor;


public class NoNameMatchedLocalDefiner extends ZeroValueLocalDefiner {

  /**
   * If the source does not have a field with the given name, define a zero value.
   */
  @Override
  public boolean defineInternal(
      @NotNull MethodVisitor v,
      @NotNull ConverterDefinition converter,
      @NotNull LocalDefinition local,
      @NotNull LocalsDefinitionContext context
  ) {
    if (!context.getProviders().containsKey(local.getName())) {
      return super.defineInternal(v, converter, local, context);
    }
    return false;
  }
}
