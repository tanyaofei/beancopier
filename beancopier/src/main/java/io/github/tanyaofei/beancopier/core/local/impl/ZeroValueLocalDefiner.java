package io.github.tanyaofei.beancopier.core.local.impl;

import io.github.tanyaofei.beancopier.constants.TypedOpcode;
import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalDefiner;
import io.github.tanyaofei.beancopier.core.local.LocalDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalsDefinitionContext;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.MethodVisitor;


/**
 * A definer for defining a zero value for specified type
 *
 * @author tanyaofei
 * @since 0.2.0
 */
public class ZeroValueLocalDefiner extends LocalDefiner {

  /**
   * Define a zero value.
   */
  @Override
  protected boolean defineInternal(
      @NotNull MethodVisitor v,
      @NotNull ConverterDefinition converter,
      @NotNull LocalDefinition local,
      @NotNull LocalsDefinitionContext context
  ) {
    var rawType = local.getType().getRawType();
    TypedOpcode.ofType(rawType).constZero(v);
    storeLocal(v, rawType, context);
    return true;
  }

}
