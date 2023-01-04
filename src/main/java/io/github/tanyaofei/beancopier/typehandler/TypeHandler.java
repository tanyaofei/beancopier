package io.github.tanyaofei.beancopier.typehandler;

/**
 * 类型处理器
 *
 * @param <S> 拷贝来源字段类型
 * @param <T> 拷贝目标字段类型
 * @since 0.1.0
 */
public abstract class TypeHandler<S, T> {

    public static final String METHOD_NAME = "handle";

    public abstract T handle(S value);

}
