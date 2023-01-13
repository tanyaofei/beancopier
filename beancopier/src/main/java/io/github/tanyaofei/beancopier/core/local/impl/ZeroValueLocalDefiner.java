package io.github.tanyaofei.beancopier.core.local.impl;

import io.github.tanyaofei.beancopier.constants.LocalOpcode;
import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalDefiner;
import io.github.tanyaofei.beancopier.core.local.LocalDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalsDefinitionContext;
import org.objectweb.asm.MethodVisitor;

/**
 * 零值局部变量定义器
 *
 * @author tanyaofei
 * @since 0.2.0
 */
public class ZeroValueLocalDefiner extends LocalDefiner {

  /**
   * 定义一个零值
   * {@inheritDoc}
   */
  @Override
  protected boolean defineInternal(
      MethodVisitor v,
      ConverterDefinition converterDefinition,
      LocalDefinition localDefinition,
      LocalsDefinitionContext context
  ) {
    var requiredType = localDefinition.getType();
    LocalOpcode.ofType(requiredType).constZero(v);
    storeLocal(v, requiredType, context);
    return true;
  }

}
