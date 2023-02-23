package io.github.tanyaofei.beancopier.converter;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
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
  public final List<T> convertAllToList(@Nullable Iterable<S> sources) {
    return convertAll(sources, Collectors.toList());
  }

  /**
   * Convert sources to targets
   *
   * @param sources sources
   * @return targets
   */
  @Nullable
  public final Set<T> convertAllToSet(@Nullable Iterable<S> sources) {
    return convertAll(sources, Collectors.toSet());
  }

  /**
   * Convert sources to targets
   *
   * @param sources sources
   * @return targets
   */
  @Nullable
  public final ArrayList<T> convertAllToArrayList(@Nullable Iterable<S> sources) {
    return convertAll(sources, Collectors.toCollection(ArrayList::new));
  }

  /**
   * Convert sources to targets
   *
   * @param sources sources
   * @return targets
   */
  @Nullable
  public final LinkedList<T> convertAllToLinkedList(@Nullable Iterable<S> sources) {
    return convertAll(sources, Collectors.toCollection(LinkedList::new));
  }

  /**
   * Convert sources to targets
   *
   * @param sources sources
   * @return targets
   */
  @Nullable
  @Contract(value = "null, _ -> null", pure = true)
  public final <A, R> R convertAll(@Nullable Iterable<S> sources, @Nonnull Collector<T, A, R> collector) {
    if (sources == null) {
      return null;
    }
    return StreamSupport.stream(sources.spliterator(), false).map(this::convert).collect(collector);
  }


}
