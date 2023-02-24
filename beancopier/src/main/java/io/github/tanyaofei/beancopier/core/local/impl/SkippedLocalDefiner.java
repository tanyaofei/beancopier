package io.github.tanyaofei.beancopier.core.local.impl;

import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalsDefinitionContext;
import org.objectweb.asm.MethodVisitor;

import javax.annotation.Nonnull;

public class SkippedLocalDefiner extends ZeroValueLocalDefiner {

  /**
   * If the field is set to skip=true, define a zero value.
   * {@inheritDoc}
   */
  @Override
  protected boolean defineInternal(
      @Nonnull MethodVisitor v,
      @Nonnull ConverterDefinition converter,
      @Nonnull LocalDefinition local,
      @Nonnull LocalsDefinitionContext context
  ) {
    if (local.isSkip()) {
      return super.defineInternal(v, converter, local, context);
    }
    return false;
  }
}
