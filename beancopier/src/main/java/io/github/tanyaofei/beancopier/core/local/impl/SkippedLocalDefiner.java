package io.github.tanyaofei.beancopier.core.local.impl;

import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalsDefinitionContext;
import org.objectweb.asm.MethodVisitor;

public class SkippedLocalDefiner extends ZeroValueLocalDefiner {

  /**
   * 如果该字段设置为 skip=true, 则定义一个零值
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
