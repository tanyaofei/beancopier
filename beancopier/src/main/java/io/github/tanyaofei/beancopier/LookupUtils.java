package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.utils.BytecodeUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @author tanyaofei
 * @since 0.2.0
 */
public class LookupUtils {

  private static final byte[] HOOK_BYTECODE = BytecodeUtils.toBytecode(LookupHook.class);

  /**
   * Return a {@link MethodHandles.Lookup} that in the specified module.
   * Provide a function to define the hook bytecode, and the hook can return a {@link java.lang.invoke.MethodHandles.Lookup} which is in the module of the classloader who define it.
   *
   * @param classLoader which classloader is about to define a hook, provide this parma can avoid defining {@link LookupHook} twice
   * @param defineClass a function to define a {@link LookupHook} class
   * @return a lookup in the specified module
   * @throws NullPointerException  if any parameter is {@link null}
   * @throws IllegalStateException if any exception occurred during the method
   */
  @Nonnull
  public static <C extends ClassLoader> MethodHandles.Lookup lookupInModule(
      @NotNull C classLoader,
      @NotNull BiFunction<@NotNull C, byte @NotNull [], @NotNull Class<?>> defineClass
  ) {
    Class<?> lookupHookClass = null;
    try {
      var loaded = classLoader.loadClass(LookupHook.class.getName());
      if (loaded.getClassLoader() == classLoader) {
        // this hook is defined by its parent classloader
        lookupHookClass = loaded;
      }
    } catch (ClassNotFoundException ignored) {

    }

    if (lookupHookClass == null) {
      lookupHookClass = Objects.requireNonNull(defineClass.apply(classLoader, getLookupHookBytecode()));
    }

    try {
      var handle = MethodHandles.lookup().findStatic(
          lookupHookClass,
          "getLookup",
          LookupHook.GET_LOOKUP_METHOD_TYPE
      );
      return (MethodHandles.Lookup) handle.invoke();
    } catch (Throwable e) {
      throw new IllegalStateException(e);
    }
  }

  @Nonnull
  public static byte[] getLookupHookBytecode() {
    return HOOK_BYTECODE;
  }

  public static class LookupHook {

    private static final MethodType GET_LOOKUP_METHOD_TYPE = MethodType.methodType(MethodHandles.Lookup.class);

    public static MethodHandles.Lookup getLookup() {
      return MethodHandles.lookup();
    }
  }


}
