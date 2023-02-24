package io.github.tanyaofei.beancopier.exception;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus.Internal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author tanyaofei
 * @since 0.2.0
 */
public non-sealed class DefineClassError extends CodeError {

  @Getter
  @Nonnull
  private final byte[] bytecode;

  @Internal
  public <S, T> DefineClassError(@Nonnull byte[] bytecode, @Nonnull Class<S> source, @Nonnull Class<T> target, @Nullable Throwable e) {
    super(source, target, e);
    this.bytecode = bytecode;
  }

}
