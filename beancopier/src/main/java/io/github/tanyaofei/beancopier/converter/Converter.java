package io.github.tanyaofei.beancopier.converter;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;

/**
 * A converter the used to copy properties of source to target.
 * The implementations class will be dynamically generated at runtime by {@link io.github.tanyaofei.beancopier.core.ConverterFactory} using ASM technology.
 *
 * @param <S> The type of source
 * @param <T> The type of target
 * @author tanyaofei
 * @since 0.0.1
 */
public interface Converter<S, T> {

  MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

  /**
   * <ol>
   *   <li>Return null if source is null</li>
   *   <li>Return a target instance that has copied fields from source</li>
   * </ol>
   *
   * @param source An object that used to copy
   * @return target
   */
  @Contract(value = "null -> null", pure = true)
  T convert(@Nullable S source);

}
