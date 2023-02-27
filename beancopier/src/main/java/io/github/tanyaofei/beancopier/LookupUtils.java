package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.utils.BytecodeUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.ref.SoftReference;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Lookup Utils for getting {@link java.lang.invoke.MethodHandles.Lookup}
 *
 * @author tanyaofei
 * @since 0.2.0
 */
public class LookupUtils {

  /**
   * Soft reference of {@link LookupHook} bytecode
   *
   * @see #getLookupHookBytecode()
   */
  private static SoftReference<byte[]> lookupHookBytecode = new SoftReference<>(null);

  /**
   * Return a {@link MethodHandles.Lookup} that can access classes loaded by the given {@code classLoader}.
   * This method will try to define a {@link LookupHook} by the given {@code classLoader},
   * so the {@link LookupHook#getLookup()} can return a {@link java.lang.invoke.MethodHandles.Lookup} that can access classes loaded by the {@code classLoader}
   * <pre>{@code
   *  MyClassLoader cl = (MyClassLoader) target.getClass().getClassLoader();
   *  MethodHandlers.Lookup lookup = LookupUtils.lookupInModules(cl, MyClassLoader::defineClass)
   *  BeanCopierImpl beancopier = new BeanCopier(features -> features.lookup(lookup));
   * }</pre>
   *
   * @param classLoader which classloader is about to define a hook
   * @param defineClass a function to define a {@link LookupHook} class, its parameters are {@code classLoader}, name, offset, length
   * @return a lookup that can access classes loaded by {@code classLoader}
   * @throws NullPointerException  if any parameter is {@code null}
   * @throws IllegalStateException should never happen
   */
  public static <C extends ClassLoader> MethodHandles.Lookup lookupInClassLoader(
      @NotNull C classLoader,
      @NotNull QuintFunction<C, String, byte[], Integer, Integer, Class<?>> defineClass // classLoader, name, code, offset, length
  ) {
    Class<?> lookupHookClass;
    try {
      lookupHookClass = getLoadedClass(LookupHook.class.getName(), classLoader);
    } catch (ClassNotFoundException e) {
      var code = getLookupHookBytecode();
      lookupHookClass = Objects.requireNonNull(
          defineClass.apply(classLoader, LookupHook.class.getName(), code, 0, code.length),
          "defineClass return null"
      );
    }

    try {
      var handle = MethodHandles.lookup().findStatic(
          lookupHookClass,
          LookupHook.GET_LOOKUP_METHOD_NAME,
          LookupHook.GET_LOOKUP_METHOD_TYPE
      );
      return (MethodHandles.Lookup) handle.invoke();
    } catch (Throwable e) {
      throw new IllegalStateException("should never happen", e);
    }
  }


  /**
   * @see #lookupInClassLoader(ClassLoader, QuintFunction)
   */
  @Nonnull
  public static <C extends ClassLoader> MethodHandles.Lookup lookupInClassLoader(
      @NotNull C classLoader,
      @NotNull BiFunction<C, byte[], Class<?>> defineClass  // classLoader, code
  ) {
    return lookupInClassLoader(classLoader, (cl, name, code, offset, length) -> defineClass.apply(cl, code));
  }

  @Nonnull
  public static byte[] getLookupHookBytecode() {
    var code = lookupHookBytecode.get();
    if (code == null) {
      code = BytecodeUtils.toBytecode(LookupHook.class);
      lookupHookBytecode = new SoftReference<>(code);
    }
    return code;
  }

  /**
   * Return a loaded class named the given {@code name}
   *
   * @param name        class name
   * @param classLoader class loader
   * @return class
   * @throws ClassNotFoundException if the class wasn't loaded or was loaded by the parent of the given {@code classloader}
   */
  private static Class<?> getLoadedClass(String name, ClassLoader classLoader) throws ClassNotFoundException {
    var clazz = classLoader.loadClass(name);
    if (clazz.getClassLoader() != classLoader) {
      throw new ClassNotFoundException("class " + name + " wasn't loaded by the given classLoader: " + classLoader + " named " + classLoader.getName());
    }
    return clazz;
  }

  /**
   * Quint-Function
   */
  @FunctionalInterface
  public interface QuintFunction<T, U, V, W, X, R> {
    R apply(T t, U u, V v, W w, X x);
  }

  public static class LookupHook {

    private static final String GET_LOOKUP_METHOD_NAME = "getLookup";
    private static final MethodType GET_LOOKUP_METHOD_TYPE = MethodType.methodType(MethodHandles.Lookup.class);

    public static MethodHandles.Lookup getLookup() {
      return MethodHandles.lookup();
    }

  }


}
