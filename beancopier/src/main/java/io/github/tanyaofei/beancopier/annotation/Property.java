package io.github.tanyaofei.beancopier.annotation;


import java.lang.annotation.*;

/**
 * @author tanyaofei
 * @since 0.1.0
 */
@Documented
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {

  /**
   *
   * <pre>{@code
   *  public class Source {
   *      private String val;
   *  }
   *
   *  public class Target {
   *      @Property(value = "val")
   *      private String value;
   *  }
   * }
   * </pre>
   *
   * <p>It will <b>NOT</b> take effect when the class of source and the class of target is the same one</p>
   *
   * @return property alias
   * @since 0.1.0
   */
  String value() default "";

  /**
   * <pre>{@code
   *  public class Source {
   *    private String val;
   *  }
   *
   *  public class Target {
   *    @Property(alias = @Alias(value = "val", forType=Source.class))
   *    private String val2;
   *  }
   * }</pre>
   * <p>It will <b>NOT</b> take effect when the class of source and the class of target is the same one</p>
   *
   * @return property alias for specified source classes
   * @since 0.2.0
   */
  Alias[] alias() default {};

  /**
   * @return skip This field will not be initial by converter if true
   * @since 0.1.0
   */
  boolean skip() default false;

}
