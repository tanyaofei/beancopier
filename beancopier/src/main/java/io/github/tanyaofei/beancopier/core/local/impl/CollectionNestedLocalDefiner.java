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
import java.util.Collection;

import static io.github.tanyaofei.beancopier.constants.MethodInvokers.AbstractConverter$convertAll;

/**
 * A definer for define a nested field variable.
 * <pre>{@code
 *  public record Source(List<Source> nested) {}
 *  public record Target(List<Target> nested) {}
 * }</pre>
 * In this case, the field named "nested" is a Collection Nested Field,
 * which means that a recursive bytecode will be generated to complete the local variable definition.
 *
 * <pre>{@code
 *  List<Target> nested = source.nested() == null
 *    ? null
 *    : source.nested().map(this::convert).collect(Collector.toList());
 * }
 * </pre>
 *
 * @author tanyaofei
 * @see NestedLocalDefiner
 */
public class CollectionNestedLocalDefiner extends LocalDefiner {

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

    if (converterDefinition.getConfiguration().isPreferNested()
        && !isCollectionNested(
        converterDefinition,
        member,
        localDefinition.getType(),
        localDefinition.getGenericType()
    )) {
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


  protected boolean isCollectionNested(ConverterDefinition converterDefinition, BeanMember souceBeanMember, Class<?> localType, Type localGenericType) {
    var sc = converterDefinition.getSourceType();
    var tc = converterDefinition.getTargetType();

    if (converterDefinition.getConfiguration().isFullTypeMatching()) {
      return souceBeanMember.getGenericType().equals(localGenericType)
          && Collection.class.isAssignableFrom(localType)
          && getCollectionElementType(souceBeanMember.getGenericType()) == sc
          && getCollectionElementType(localGenericType) == tc;
    } else {
      return Collection.class.isAssignableFrom(souceBeanMember.getType())
          && Collection.class.isAssignableFrom(localType)
          && getCollectionElementType(souceBeanMember.getGenericType()) == sc
          && getCollectionElementType(localGenericType) == tc;
    }

  }

  /**
   * 获取指定字段 {@link Collection} 的元素类型
   *
   * @param type 集合类
   * @return 集合的元素类型
   */
  @SuppressWarnings("unchecked")
  private Class<?> getCollectionElementType(Type type) {
    return ((TypeToken<? extends Collection<?>>) TypeToken.of(type))
        .getSupertype(Collection.class)
        .resolveType(Collection.class.getTypeParameters()[0])
        .getRawType();
  }


}
