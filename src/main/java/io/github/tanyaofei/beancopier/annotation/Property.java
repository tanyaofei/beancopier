package io.github.tanyaofei.beancopier.annotation;


import io.github.tanyaofei.beancopier.typehandler.TypeHandler;

import java.lang.annotation.*;

/**
 * 字段设置
 *
 * @since 0.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Property {

    /**
     * 字段别名
     * <pre>
     * {@code
     *  public class Source {
     *      private String val;
     *  }
     *
     *  public class Target {
     *      @Property(value = "val")
     *      private String value;
     *  }
     *
     *  Source source = new Source();
     *  source.setVal("string");
     *  Target target = BeanCopier.copy(source);
     *  assert source.getVal().equals(target.getValue());
     * }
     * </pre>
     *
     * <p>在拷贝目标字段上使用, 会在拷贝来源中查找同类型的指定名称字段</p>
     */
    String value() default "";

    /**
     * <ul>
     *     <li>handle 方法必须在当类实现(BeanCopier 通过 getDeclaredMethod 获取)</li>
     *     <li>handle 方法不能包含泛型参数, 因为需要通过参数类型和返回值类型来判断该处理器是否使用</li>
     * </ul>
     *
     * @return 类型处理器
     * @see io.github.tanyaofei.beancopier.typehandler.impl.listtoarray.BooleanListToArrayTypeHandler
     * @see io.github.tanyaofei.beancopier.typehandler.impl.listtoarray.IntegerListToArrayTypeHandler
     * @see io.github.tanyaofei.beancopier.typehandler.impl.listtoarray.LongListToArrayTypeHandler
     * @see io.github.tanyaofei.beancopier.typehandler.impl.listtoarray.ShortListToArrayTypeHandler
     * @see io.github.tanyaofei.beancopier.typehandler.impl.listtoarray.StringListToArrayTypeHandler
     * @see io.github.tanyaofei.beancopier.typehandler.impl.arraytolist.BooleanArrayToListTypeHandler
     * @see io.github.tanyaofei.beancopier.typehandler.impl.arraytolist.IntegerArrayToListTypeHandler
     * @see io.github.tanyaofei.beancopier.typehandler.impl.arraytolist.LongArrayToListTypeHandler
     * @see io.github.tanyaofei.beancopier.typehandler.impl.arraytolist.ShortArrayToListTypeHandler
     * @see io.github.tanyaofei.beancopier.typehandler.impl.arraytolist.StringArrayToListTypeHandler
     */
    Class<? extends TypeHandler<?, ?>>[] typeHandler() default {};


    /**
     * @return 拷贝特性
     */
    Feature[] feature() default {};




}
