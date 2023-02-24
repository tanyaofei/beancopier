package io.github.tanyaofei.beancopier.core.local.impl;

import io.github.tanyaofei.beancopier.constants.TypedOpcode;
import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalDefiner;
import io.github.tanyaofei.beancopier.core.local.LocalDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalsDefinitionContext;
import org.objectweb.asm.MethodVisitor;

import javax.annotation.Nonnull;

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
      @Nonnull MethodVisitor v,
      @Nonnull ConverterDefinition converter,
      @Nonnull LocalDefinition local,
      @Nonnull LocalsDefinitionContext context
  ) {
    var rawType = local.getType().getRawType();
    TypedOpcode.ofType(rawType).constZero(v);
    storeLocal(v, rawType, context);
    return true;
  }

}
