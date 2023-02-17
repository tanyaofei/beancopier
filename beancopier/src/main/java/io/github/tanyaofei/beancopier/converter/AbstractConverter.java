package io.github.tanyaofei.beancopier.converter;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An abstract class for all {@link Converter} implementation classes generated at runtime,
 * providing some common fields to assist bytecode generation.
 *
 * @author tanyaofei
 */
public abstract class AbstractConverter<S, T> implements Converter<S, T> {

  /**
   * Convert sources to targets
   *
   * @param sources sources
   * @return targets
   */
  @Nullable
  public final List<T> convertAll(@Nullable Collection<S> sources) {
    if (sources == null) {
      return null;
    }
    return sources.stream().map(this::convert).collect(Collectors.toList());
  }

}
