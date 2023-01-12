package io.github.tanyaofei.beancopier;

import org.jetbrains.annotations.NotNull;

/**
 * @author tanyaofei
 * @since 0.0.1
 */
class DefaultClassLoader extends ClassLoader implements ConverterClassLoader {

  public DefaultClassLoader(ClassLoader parent) {
    super(parent);
  }

  @Override
  @NotNull
  public final Class<?> defineClass(String name, byte @NotNull [] code) {
    return super.defineClass(name, code, 0, code.length);
  }

}
