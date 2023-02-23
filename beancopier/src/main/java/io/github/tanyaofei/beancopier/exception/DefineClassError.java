package io.github.tanyaofei.beancopier.exception;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * @author tanyaofei
 * @since 0.2.0
 */
public non-sealed class DefineClassError extends CodeError {

  @Getter
  private final byte[] bytecode;

  @Internal
  public <S, T> DefineClassError(byte[] bytecode, Class<S> source, Class<T> target, Throwable e) {
    super(source, target, e);
    this.bytecode = bytecode;
  }

}
