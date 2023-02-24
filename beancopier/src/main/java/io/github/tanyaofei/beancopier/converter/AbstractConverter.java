package io.github.tanyaofei.beancopier.converter;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An abstract class for all {@link Converter} implementation classes generated at runtime,
 * providing some common fields to assist bytecode generation.
 *
 * @author tanyaofei
 */
public abstract class AbstractConverter<S, T> implements Converter<S, T> {

  protected Class<T> sourceType = null;
  protected Class<T> targetType = null;

  @SuppressWarnings("unchecked")
  protected Class<T> getSourceType() {
    if (sourceType == null) {
      this.sourceType = (Class<T>) getTypeArguments()[0];
    }
    return this.sourceType;
  }


  @SuppressWarnings("unchecked")
  protected Class<T> getTargetType() {
    if (targetType == null) {
      this.targetType = (Class<T>) getTypeArguments()[1];
    }
    return this.targetType;
  }


  private Type[] getTypeArguments() {
    return ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments();
  }

  protected final Stream<T> convertArrayToStream(S[] sources) {
    if (sources == null) {
      return null;
    }
    return Stream.of(sources).map(this::convert);
  }

  protected final Stream<T> convertIterableToStream(Iterable<S> sources) {
    if (sources == null) {
      return null;
    }
    return StreamSupport.stream(sources.spliterator(), false).map(this::convert);
  }

  protected final Set<T> collectToSet(Stream<T> targets) {
    if (targets == null) {
      return null;
    }
    return targets.collect(Collectors.toSet());
  }

  protected final HashSet<T> collectToHashSet(Stream<T> targets) {
    if (targets == null) {
      return null;
    }
    return targets.collect(Collectors.toCollection(HashSet::new));
  }

  protected final TreeSet<T> collectToTreeSet(Stream<T> targets) {
    if (targets == null) {
      return null;
    }
    return targets.collect(Collectors.toCollection(TreeSet::new));
  }

  protected final List<T> collectToList(Stream<T> targets) {
    if (targets == null) {
      return null;
    }
    return targets.collect(Collectors.toList());
  }

  protected final LinkedList<T> collectToLinkedList(Stream<T> targets) {
    if (targets == null) {
      return null;
    }
    return targets.collect(Collectors.toCollection(LinkedList::new));
  }

  protected final ArrayList<T> collectToArrayList(Stream<T> targets) {
    if (targets == null) {
      return null;
    }
    return targets.collect(Collectors.toCollection(ArrayList::new));
  }

  protected final T[] collectToArray(Stream<T> targets) {
    if (targets == null) {
      return null;
    }
    return targets.toArray(size -> (T[]) Array.newInstance(getTargetType(), size));
  }


  @Nullable
  public final Stream<T> convertAllToStream(@Nullable Iterable<S> sources) {
    if (sources == null) {
      return null;
    }
    return StreamSupport.stream(sources.spliterator(), false).map(this::convert);
  }

}
