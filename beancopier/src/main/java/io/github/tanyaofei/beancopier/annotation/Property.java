package io.github.tanyaofei.beancopier.annotation;


import java.lang.annotation.*;

/**
 * 字段配置
 *
 * @author tanyaofei
 * @since 0.1.0
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {

  /**
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
   * <p>it will not take effect when the class of source and the class of target is the same one</p>
   *
   * @return property alias
   * @since 0.1.0
   */
  String value() default "";

  /**
   * @return skip This field will not be initial by converter if true
   * @since 0.1.0
   */
  boolean skip() default false;

}
