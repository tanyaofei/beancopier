package io.github.tanyaofei.beancopier.converter;

import io.github.tanyaofei.guava.common.reflect.TypeToken;
import org.jetbrains.annotations.Contract;

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

  private Type[] typeArguments;

  @SuppressWarnings("unchecked")
  protected Class<S> getSourceType() {
    if (typeArguments == null) {
      typeArguments = getTypeArguments();
    }
    return (Class<S>) typeArguments[0];
  }

  @SuppressWarnings("unchecked")
  protected Class<T> getTargetType() {
    if (typeArguments == null) {
      typeArguments = getTypeArguments();
    }
    return (Class<T>) typeArguments[1];
  }

  private Type[] getTypeArguments() {
    var clazz = this.getClass();
    if (clazz.getSuperclass() == AbstractConverter.class) {
      return ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
    }

    var typeParameters = AbstractConverter.class.getTypeParameters();
    var typeArguments = new Type[typeParameters.length];
    var supertype = TypeToken
        .of(clazz)
        .getSupertype(AbstractConverter.class);

    for(int i = 0; i < typeParameters.length; i++) {
      typeArguments[i] = supertype.resolveType(typeParameters[i]).getRawType();
    }
    return typeArguments;
  }

  @Contract(value = "null -> null", pure = true)
  protected final Stream<T> convertArrayToStream(S[] sources) {
    if (sources == null) {
      return null;
    }
    return Stream.of(sources).map(this::convert);
  }

  @Contract(value = "null -> null", pure = true)
  protected final Stream<T> convertIterableToStream(Iterable<S> sources) {
    if (sources == null) {
      return null;
    }
    return StreamSupport.stream(sources.spliterator(), false).map(this::convert);
  }

  @Contract(value = "null -> null")
  protected final Set<T> collectToSet(Stream<T> targets) {
    if (targets == null) {
      return null;
    }
    return targets.collect(Collectors.toSet());
  }

  @Contract(value = "null -> null")
  protected final HashSet<T> collectToHashSet(Stream<T> targets) {
    if (targets == null) {
      return null;
    }
    return targets.collect(Collectors.toCollection(HashSet::new));
  }

  @Contract(value = "null -> null")
  protected final TreeSet<T> collectToTreeSet(Stream<T> targets) {
    if (targets == null) {
      return null;
    }
    return targets.collect(Collectors.toCollection(TreeSet::new));
  }

  @Contract(value = "null -> null")
  protected final List<T> collectToList(Stream<T> targets) {
    if (targets == null) {
      return null;
    }
    return targets.collect(Collectors.toList());
  }

  @Contract(value = "null -> null")
  protected final LinkedList<T> collectToLinkedList(Stream<T> targets) {
    if (targets == null) {
      return null;
    }
    return targets.collect(Collectors.toCollection(LinkedList::new));
  }

  @Contract(value = "null -> null")
  protected final ArrayList<T> collectToArrayList(Stream<T> targets) {
    if (targets == null) {
      return null;
    }
    return targets.collect(Collectors.toCollection(ArrayList::new));
  }

  @Contract(value = "null -> null")
  @SuppressWarnings("unchecked")
  protected final T[] collectToArray(Stream<T> targets) {
    if (targets == null) {
      return null;
    }
    return targets.toArray(size -> (T[]) Array.newInstance(getTargetType(), size));
  }

}
