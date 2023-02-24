package io.github.tanyaofei.beancopier;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author tanyaofei
 */
@Data
@Accessors(chain = true)
public class BeanCopierConfiguration {

  /**
   * Determine a disk path that the {@link io.github.tanyaofei.beancopier.converter.Converter} classes will be dumped to.
   * {@code null} or an empty value indicates that no dump will be performed.
   * <br>
   * No exceptions will be thrown during the dumping of the class.
   * <pre>{@code
   *  if (debugLocation != null && !debugLocation.isBlank()) {
   *    try {
   *      // dump converter class to disk
   *    } catch (Throwable e) {
   *      e.printStacktrace();
   *    }
   *  }
   * }</pre>
   */
  private String debugLocation = System.getProperty("beancopier.debugLocation");

}
