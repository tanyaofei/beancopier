package io.github.tanyaofei.beancopier.converter;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
  public final List<T> convertAll(@Nullable Iterable<S> sources) {
    if (sources == null) {
      return null;
    }
    return StreamSupport.stream(sources.spliterator(), false).map(this::convert).collect(Collectors.toList());
  }

}
