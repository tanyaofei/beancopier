package io.github.tanyaofei.beancopier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Converter ClassLoader.
 * Using the implementations class of this interface can be faster to define class.
 *
 * @author tanyaofei
 * @since 0.1.4
 */
public interface ConverterClassLoader {

  /**
   * 加载类
   *
   * @param name 类名
   * @param code 字节码字节数组
   * @return 类
   */
  @NotNull
  Class<?> defineClass(@Nullable String name, byte @NotNull [] code);

}
