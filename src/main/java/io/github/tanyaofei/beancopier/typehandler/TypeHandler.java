package io.github.tanyaofei.beancopier.typehandler;


import org.jetbrains.annotations.NotNull;

/**
 * 类型处理器
 *
 * @param <S> 拷贝来源字段类型
 * @param <T> 拷贝目标字段类型
 * @since 0.1.0
 */
public abstract class TypeHandler<S, T> {

  public abstract T handle(@NotNull S value);

}
