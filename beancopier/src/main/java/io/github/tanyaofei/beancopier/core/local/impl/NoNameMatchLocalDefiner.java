package io.github.tanyaofei.beancopier.core.local.impl;

import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalsDefinitionContext;
import org.objectweb.asm.MethodVisitor;

public class NoNameMatchLocalDefiner extends ZeroValueLocalDefiner {

  /**
   * If the source does not have a field with the given name, define a zero value.
   */
  @Override
  public boolean defineInternal(
      MethodVisitor v,
      ConverterDefinition converterDefinition,
      LocalDefinition localDefinition,
      LocalsDefinitionContext context
  ) {
    if (!context.getSourceMembers().containsKey(localDefinition.getName())) {
      return super.defineInternal(v, converterDefinition, localDefinition, context);
    }
    return false;
  }
}
