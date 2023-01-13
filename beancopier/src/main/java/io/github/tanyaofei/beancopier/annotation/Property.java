package io.github.tanyaofei.beancopier.annotation;


import java.lang.annotation.*;

/**
 * 字段配置
 *
 * @author tanyaofei
 * @since 0.1.0
 */
@Documented
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {

  /**
   * 字段别名
   * <pre>
   *  public class Source {
   *      private String val;
   *  }
   *
   *  public class Target {
   *     {@literal @}Property(value = "val")
   *      private String value;
   *  }
   *
   *  Source source = new Source();
   *  source.setVal("string");
   *  Target target = BeanCopier.copy(source);
   *  assert source.getVal().equals(target.getValue());
   * </pre>
   *
   * <p>在拷贝目标字段上使用, 会在拷贝来源中查找同类型的指定名称字段</p>
   * <p>这个配置对拷贝来源和拷贝目标是同一个类时不生效</p>
   * @return 字段别名
   * @since 0.1.0
   */
  String value() default "";

  /**
   * 为 {@code true} 时不对此字段进行 set, 该字段将使用构造函数里指定默认值
   *
   * @return 是否跳过此字段;
   * @since 0.1.0
   */
  boolean skip() default false;

}
