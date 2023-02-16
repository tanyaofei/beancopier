package io.github.tanyaofei.beancopier.utils.reflection;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author tanyaofei
 */
public class ClassUtils {

  public static final String ARRAY_SUFFIX = "[]";

  public static final String CGLIB_CLASS_SEPARATOR = "$$";

  public static final String CLASS_FILE_SUFFIX = ".class";

  private static final String INTERNAL_ARRAY_PREFIX = "[";

  private static final String NON_PRIMITIVE_ARRAY_PREFIX = "[L";

  private static final char PACKAGE_SEPARATOR = '.';

  private static final char PATH_SEPARATOR = '/';

  private static final char INNER_CLASS_SEPARATOR = '$';

  private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new IdentityHashMap<Class<?>, Class<?>>(8);

  private static final Map<String, Class<?>> primitiveTypeNameMap = new HashMap<String, Class<?>>(32);

  private static final Map<String, Class<?>> commonClassCache = new HashMap<String, Class<?>>(32);


  static {
    primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
    primitiveWrapperTypeMap.put(Byte.class, byte.class);
    primitiveWrapperTypeMap.put(Character.class, char.class);
    primitiveWrapperTypeMap.put(Double.class, double.class);
    primitiveWrapperTypeMap.put(Float.class, float.class);
    primitiveWrapperTypeMap.put(Integer.class, int.class);
    primitiveWrapperTypeMap.put(Long.class, long.class);
    primitiveWrapperTypeMap.put(Short.class, short.class);

    for (Map.Entry<Class<?>, Class<?>> entry : primitiveWrapperTypeMap.entrySet()) {
      registerCommonClasses(entry.getKey());
    }

    Set<Class<?>> primitiveTypes = new HashSet<Class<?>>(64);
    primitiveTypes.addAll(primitiveWrapperTypeMap.values());
    primitiveTypes.addAll(Arrays.asList(new Class<?>[]{
        boolean[].class, byte[].class, char[].class, double[].class,
        float[].class, int[].class, long[].class, short[].class}));
    primitiveTypes.add(void.class);
    for (Class<?> primitiveType : primitiveTypes) {
      primitiveTypeNameMap.put(primitiveType.getName(), primitiveType);
    }

    registerCommonClasses(Boolean[].class, Byte[].class, Character[].class, Double[].class,
        Float[].class, Integer[].class, Long[].class, Short[].class);
    registerCommonClasses(Number.class, Number[].class, String.class, String[].class,
        Class.class, Class[].class, Object.class, Object[].class);
    registerCommonClasses(Throwable.class, Exception.class, RuntimeException.class,
        Error.class, StackTraceElement.class, StackTraceElement[].class);
    registerCommonClasses(Enum.class, Iterable.class, Cloneable.class, Comparable.class);
  }

  public static Class<?> resolveClassName(String className, ClassLoader classLoader) throws IllegalArgumentException {
    try {
      return forName(className, classLoader);
    } catch (ClassNotFoundException ex) {
      throw new IllegalArgumentException("Could not find class [" + className + "]", ex);
    } catch (LinkageError err) {
      throw new IllegalArgumentException("Unresolvable class definition for class [" + className + "]", err);
    }
  }

  private static void registerCommonClasses(Class<?>... commonClasses) {
    for (Class<?> clazz : commonClasses) {
      commonClassCache.put(clazz.getName(), clazz);
    }
  }

  public static String getClassFileName(Class<?> c) {
    String className = c.getName();
    int lastDotIndex = className.lastIndexOf(".");
    return className.substring(lastDotIndex + 1) + ".class";
  }


  public static Class<?> forName(String name, ClassLoader classLoader) throws ClassNotFoundException, LinkageError {
    Class<?> clazz = resolvePrimitiveClassName(name);
    if (clazz == null) {
      clazz = commonClassCache.get(name);
    }
    if (clazz != null) {
      return clazz;
    }

    // "java.lang.String[]" style arrays
    if (name.endsWith(ARRAY_SUFFIX)) {
      String elementClassName = name.substring(0, name.length() - ARRAY_SUFFIX.length());
      Class<?> elementClass = forName(elementClassName, classLoader);
      return Array.newInstance(elementClass, 0).getClass();
    }

    // "[Ljava.lang.String;" style arrays
    if (name.startsWith(NON_PRIMITIVE_ARRAY_PREFIX) && name.endsWith(";")) {
      String elementName = name.substring(NON_PRIMITIVE_ARRAY_PREFIX.length(), name.length() - 1);
      Class<?> elementClass = forName(elementName, classLoader);
      return Array.newInstance(elementClass, 0).getClass();
    }

    // "[[I" or "[[Ljava.lang.String;" style arrays
    if (name.startsWith(INTERNAL_ARRAY_PREFIX)) {
      String elementName = name.substring(INTERNAL_ARRAY_PREFIX.length());
      Class<?> elementClass = forName(elementName, classLoader);
      return Array.newInstance(elementClass, 0).getClass();
    }

    ClassLoader clToUse = classLoader;
    if (clToUse == null) {
      clToUse = getDefaultClassLoader();
    }
    try {
      return (clToUse != null ? clToUse.loadClass(name) : Class.forName(name));
    } catch (ClassNotFoundException ex) {
      int lastDotIndex = name.lastIndexOf(PACKAGE_SEPARATOR);
      if (lastDotIndex != -1) {
        String innerClassName =
            name.substring(0, lastDotIndex) + INNER_CLASS_SEPARATOR + name.substring(lastDotIndex + 1);
        try {
          return (clToUse != null ? clToUse.loadClass(innerClassName) : Class.forName(innerClassName));
        } catch (ClassNotFoundException ex2) {
          // Swallow - let original exception get through
        }
      }
      throw ex;
    }
  }

  public static Class<?> resolvePrimitiveClassName(String name) {
    Class<?> result = null;
    // Most class names will be quite long, considering that they
    // SHOULD sit in a package, so a length check is worthwhile.
    if (name != null && name.length() <= 8) {
      // Could be a primitive - likely.
      result = primitiveTypeNameMap.get(name);
    }
    return result;
  }

  public static ClassLoader getDefaultClassLoader() {
    ClassLoader cl = null;
    try {
      cl = Thread.currentThread().getContextClassLoader();
    } catch (Throwable ex) {
      // Cannot access thread context ClassLoader - falling back...
    }
    if (cl == null) {
      // No thread context class loader -> use class loader of this class.
      cl = ClassUtils.class.getClassLoader();
      if (cl == null) {
        // getClassLoader() returning null indicates the bootstrap ClassLoader
        try {
          cl = ClassLoader.getSystemClassLoader();
        } catch (Throwable ex) {
          // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
        }
      }
    }
    return cl;
  }

}
