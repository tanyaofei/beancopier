package io.github.tanyaofei.beancopier.exception;

import org.jetbrains.annotations.ApiStatus.Internal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author tanyaofei
 * @since 0.2.0
 */
public sealed class CodeError extends BeanCopierError permits DefineClassError, InstantiationError {

  @Internal
  public CodeError(@Nullable String message, @Nullable Throwable cause) {
    super(message, cause);
  }

  @Internal
  public CodeError(@Nonnull Class<?> source,@Nonnull Class<?> target, @Nullable Throwable cause) {
    this("Failed to generate converter bytecode: " + source.getName() + " -> " + target.getName(), cause);
  }

}
