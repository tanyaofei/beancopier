package io.github.tanyaofei.beancopier.core.local.impl;

import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.invoker.ExecutableInvoker;
import io.github.tanyaofei.beancopier.core.invoker.MethodInvoker;
import io.github.tanyaofei.beancopier.core.local.LocalDefiner;
import io.github.tanyaofei.beancopier.core.local.LocalDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalsDefinitionContext;
import io.github.tanyaofei.beancopier.utils.GenericType;
import io.github.tanyaofei.beancopier.utils.reflection.member.BeanMember;
import io.github.tanyaofei.guava.common.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.*;

import static io.github.tanyaofei.beancopier.constants.Invokers.*;

/**
 * A definer for define a nested field variable.
 * <pre>{@code
 *  public record Source(List<Source> nested) {}
 *  public record Target(List<Target> nested) {}
 * }</pre>
 * In this case, the field named "nested" is an Iterable Nested Field,
 * which means that a recursive bytecode will be generated to complete the local variable definition.
 *
 * <pre>{@code
 *  List<Target> nested = this.convertAll(source.getNested());
 * }
 * </pre>
 *
 * @author tanyaofei
 * @see NestedLocalDefiner
 */
public class IterableNestedLocalDefiner extends LocalDefiner {

  @Override
  protected boolean defineInternal(
      @Nonnull MethodVisitor v,
      @Nonnull ConverterDefinition converter,
      @Nonnull LocalDefinition local,
      @Nonnull LocalsDefinitionContext context
  ) {
    var config = converter.getFeatures();
    if (!config.isPreferNested()) {
      return false;
    }

    var provider = context.getProviders().get(local.getName());
    if (provider == null) {
      return false;
    }

    if (!isIterableNested(converter, provider, local.getType())) {
      return false;
    }
    if (!isIterableNested(converter, provider, local.getType())
    ) {
      return false;
    }

    var convertAll = getConvertAllMethodInvoker(provider.getType().getRawType());
    if (convertAll == null) {
      return false;
    }
    var collect = getCollectMethodInvoker(local.getType().getRawType());
    if (collect == null) {
      return false;
    }

    var getter = ExecutableInvoker.invoker(provider.getMethod());

    loadThis(v);
    v.visitInsn(DUP);
    loadSource(v);
    getter.invoke(v);
    convertAll.invoke(v);
    collect.invoke(v);
    v.visitTypeInsn(CHECKCAST, org.objectweb.asm.Type.getInternalName(local.getType().getRawType()));
    storeLocal(v, local, context);
    return true;
  }

  @Nullable
  protected MethodInvoker getConvertAllMethodInvoker(@Nonnull Class<?> providedType) {
    if (Iterable.class.isAssignableFrom(providedType)) {
      return AbstractConverter$convertIterableToStream;
    } else if (providedType.isArray()) {
      return AbstractConverter$convertArrayToStream;
    } else {
      return null;
    }
  }

  @Nullable
  protected MethodInvoker getCollectMethodInvoker(@Nonnull Class<?> consumerType) {
    if (consumerType.isAssignableFrom(List.class)) {
      return AbstractConverter$collectToList;
    } else if (consumerType.isAssignableFrom(ArrayList.class)) {
      return AbstractConverter$collectToArrayList;
    } else if (consumerType.isAssignableFrom(LinkedList.class)) {
      return AbstractConverter$collectToLinkedList;
    } else if (consumerType.isAssignableFrom(Set.class)) {
      return AbstractConverter$collectToSet;
    } else if (consumerType.isAssignableFrom(HashSet.class)) {
      return AbstractConverter$collectToHashSet;
    } else if (consumerType.isAssignableFrom(TreeSet.class)) {
      return AbstractConverter$collectToTreeSet;
    } else if (consumerType.isArray()) {
      return AbstractConverter$collectToArray;
    } else {
      return null;
    }
  }


  private boolean isIterableNested(
      @Nonnull ConverterDefinition converterDefinition,
      @Nonnull BeanMember provided,
      @Nonnull GenericType<?> consumerType
  ) {
    var sc = converterDefinition.getSourceType();
    var tc = converterDefinition.getTargetType();
    boolean fullTypeMatching = converterDefinition.getFeatures().isFullTypeMatching();
    if (fullTypeMatching) {
      return Iterable.class.isAssignableFrom(provided.getType().getRawType())
          && Iterable.class.isAssignableFrom(consumerType.getRawType())
          && getIterableElementType(provided.getType().getGenericType()) == sc
          && getIterableElementType(consumerType.getGenericType()) == tc;
    } else {
      var providedElementType = getElementType(provided.getType());
      if (providedElementType == null) {
        return false;
      }
      var requiredElementType = getElementType(consumerType);
      if (requiredElementType == null) {
        return false;
      }
      return providedElementType == sc && requiredElementType == tc;
    }
  }

  /**
   * Return the elementType of specified {@code type}.
   * <ol>
   *   <li>If the specified is a {@link Iterable}, return the element type of it</li>
   *   <li>If the specified is a array, return the component type of it</li>
   *   <li>return null if the specified it's not a {@link Iterable} or an array</li>
   * </ol>
   *
   * @param type a class extends from {@link Iterable} or an array class
   * @return the element type of specified type
   */
  @Nullable
  private Class<?> getElementType(@Nonnull GenericType<?> type) {
    var rawType = type.getRawType();
    if (Iterable.class.isAssignableFrom(rawType)) {
      return getIterableElementType(type.getGenericType());
    } else if (rawType.isArray()) {
      return rawType.getComponentType();
    } else {
      return null;
    }
  }


  /**
   * Return the element type of {@link Iterable}
   *
   * @param type type
   * @return the element type of {@link Iterable}
   */
  @SuppressWarnings("unchecked")
  private Class<?> getIterableElementType(@Nonnull Type type) {
    return ((TypeToken<? extends Iterable<?>>) TypeToken.of(type))
        .getSupertype(Iterable.class)
        .resolveType(Iterable.class.getTypeParameters()[0])
        .getRawType();
  }


}
