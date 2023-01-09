package io.github.tanyaofei.beancopier;

import org.jetbrains.annotations.NotNull;

/**
 * @author tanyaofei
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
