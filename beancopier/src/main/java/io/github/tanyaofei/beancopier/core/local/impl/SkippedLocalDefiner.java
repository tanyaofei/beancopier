package io.github.tanyaofei.beancopier.core.local.impl;

import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalsDefinitionContext;
import org.objectweb.asm.MethodVisitor;

public class SkippedLocalDefiner extends ZeroValueLocalDefiner {

  /**
   * If the field is set to skip=true, define a zero value.
   * {@inheritDoc}
   */
  @Override
  protected boolean defineInternal(
      MethodVisitor v,
      ConverterDefinition converter,
      LocalDefinition local,
      LocalsDefinitionContext context
  ) {
    if (local.isSkip()) {
      return super.defineInternal(v, converter, local, context);
    }
    return false;
  }
}
