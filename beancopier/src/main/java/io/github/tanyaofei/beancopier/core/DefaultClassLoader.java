package io.github.tanyaofei.beancopier.core;

import io.github.tanyaofei.beancopier.ConverterClassLoader;
import org.jetbrains.annotations.NotNull;

/**
 * @author tanyaofei
 * @since 0.0.1
 */
public class DefaultClassLoader extends ClassLoader implements ConverterClassLoader {

  public DefaultClassLoader(ClassLoader parent) {
    super(parent);
  }

  @Override
  @NotNull
  public final Class<?> defineClass(String name, byte @NotNull [] code) {
    return super.defineClass(name, code, 0, code.length);
  }

}
