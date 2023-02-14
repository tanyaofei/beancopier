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
      ConverterDefinition converterDefinition,
      LocalDefinition localDefinition,
      LocalsDefinitionContext context
  ) {
    if (localDefinition.isSkip()) {
      return super.defineInternal(v, converterDefinition, localDefinition, context);
    }
    return false;
  }
}
