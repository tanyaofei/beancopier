package io.github.tanyaofei.beancopier.core.local.impl;

import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalsDefinitionContext;
import org.objectweb.asm.MethodVisitor;

public class NoNameMatchLocalDefiner extends ZeroValueLocalDefiner {

  /**
   * 如果 source 不存在需要名称的字段，则定义一个零值
   * {@inheritDoc}
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
