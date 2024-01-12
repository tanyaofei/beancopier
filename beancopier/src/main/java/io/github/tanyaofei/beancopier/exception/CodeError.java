package io.github.tanyaofei.beancopier.exception;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @author tanyaofei
 * @since 0.2.0
 */
public class CodeError extends BeanCopierError  {

  @Internal
  public CodeError(@Nullable String message, @Nullable Throwable cause) {
    super(message, cause);
  }

  @Internal
  public CodeError(@NotNull Class<?> source, @NotNull Class<?> target, @Nullable Throwable cause) {
    this("Failed to generate converter bytecode: " + source.getName() + " -> " + target.getName(), cause);
  }

}
