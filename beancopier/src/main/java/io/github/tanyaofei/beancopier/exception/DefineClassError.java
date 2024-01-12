package io.github.tanyaofei.beancopier.exception;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @author tanyaofei
 * @since 0.2.0
 */
public class DefineClassError extends CodeError {

  @Getter
  private final byte @NotNull [] bytecode;

  @Internal
  public <S, T> DefineClassError(byte @NotNull [] bytecode, @NotNull Class<S> source, @NotNull Class<T> target, @Nullable Throwable e) {
    super(source, target, e);
    this.bytecode = bytecode;
  }

}
