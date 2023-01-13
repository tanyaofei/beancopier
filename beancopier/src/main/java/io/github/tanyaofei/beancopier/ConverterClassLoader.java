package io.github.tanyaofei.beancopier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * BeanCopier 转换器 ClassLoader 接口
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
