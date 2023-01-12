package io.github.tanyaofei.beancopier.annotation;



import java.lang.annotation.*;

/**
 * 字段配置
 *
 * @author tanyaofei
 * @since 0.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
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
   * @return 字段别名
   */
  String value() default "";

  /**
   * @return 是否跳过此字段; 为 {@code true} 时不拷贝到此字段
   */
  boolean skip() default false;

}
