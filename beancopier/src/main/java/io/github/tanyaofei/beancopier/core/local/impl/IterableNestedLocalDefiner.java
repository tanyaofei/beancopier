package io.github.tanyaofei.beancopier.core.local.impl;

import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.invoker.ExecutableInvoker;
import io.github.tanyaofei.beancopier.core.local.LocalDefiner;
import io.github.tanyaofei.beancopier.core.local.LocalDefinition;
import io.github.tanyaofei.beancopier.core.local.LocalsDefinitionContext;
import io.github.tanyaofei.beancopier.utils.reflection.member.BeanMember;
import io.github.tanyaofei.guava.common.reflect.TypeToken;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Type;

import static io.github.tanyaofei.beancopier.constants.Invokers.AbstractConverter$convertAll;

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
 * @see io.github.tanyaofei.beancopier.converter.AbstractConverter#convertAll(Iterable)
 *
 * @author tanyaofei
 * @see NestedLocalDefiner
 */
public class IterableNestedLocalDefiner extends LocalDefiner {

  @Override
  protected boolean defineInternal(
      MethodVisitor v,
      ConverterDefinition converterDefinition,
      LocalDefinition localDefinition,
      LocalsDefinitionContext context
  ) {
    if (!converterDefinition.getConfiguration().isPreferNested()) {
      return false;
    }

    var member = context.getSourceMembers().get(localDefinition.getName());
    if (member == null) {
      return false;
    }

    if (!converterDefinition.getConfiguration().isPreferNested()
        || !isIterableNested(converterDefinition, member, localDefinition.getType(), localDefinition.getGenericType())
    ) {
      return false;
    }

    var getter = ExecutableInvoker.invoker(member.getMethod());

    /*
     var variable = this.convertAll(source.getVal());
     */
    loadThis(v);
    loadSource(v);
    getter.invoke(v);
    AbstractConverter$convertAll.invoke(v);
    storeLocal(v, localDefinition, context);
    return true;
  }


  protected boolean isIterableNested(ConverterDefinition converterDefinition, BeanMember souceBeanMember, Class<?> localType, Type localGenericType) {
    if (!Iterable.class.isAssignableFrom(localType)) {
      return false;
    }
    if (!Iterable.class.isAssignableFrom(souceBeanMember.getType())) {
      return false;
    }
    if (converterDefinition.getConfiguration().isFullTypeMatching()) {
      if (localType != souceBeanMember.getType()) {
        return false;
      }
    }

    if (converterDefinition.getConfiguration().isFullTypeMatching() && localType != souceBeanMember.getType()) {
      return false;
    }

    var sc = converterDefinition.getSourceType();
    var tc = converterDefinition.getTargetType();

    return getIterableElementType(localGenericType) == tc
        && getIterableElementType(souceBeanMember.getGenericType()) == sc;
  }

  /**
   * Return the element type of {@link Iterable}
   *
   * @param type type
   * @return the element type of {@link Iterable}
   */
  @SuppressWarnings("unchecked")
  private Class<?> getIterableElementType(Type type) {
    return ((TypeToken<? extends Iterable<?>>) TypeToken.of(type))
        .getSupertype(Iterable.class)
        .resolveType(Iterable.class.getTypeParameters()[0])
        .getRawType();
  }


}
