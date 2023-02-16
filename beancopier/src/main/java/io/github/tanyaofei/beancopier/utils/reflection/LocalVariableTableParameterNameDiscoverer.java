package io.github.tanyaofei.beancopier.utils.reflection;

import lombok.var;
import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tanyaofei
 */
public class LocalVariableTableParameterNameDiscoverer {

  private final static Map<Member, String[]> NOT_FOUND = Collections.emptyMap();

  private final static Map<Class<?>, Map<Member, String[]>> parameterNamesCache = new ConcurrentHashMap<>(32);

  private static Map<Member, String[]> inspectClass(Class<?> c) {
    var in = c.getResourceAsStream(ClassUtils.getClassFileName(c));
    if (in == null) {
      return null;
    }

    try {
      var cr = new ClassReader(in);
      var map = new ConcurrentHashMap<Member, String[]>(32);
      cr.accept(new ParameterNameDiscoveringVisitor(c, map), 0);
      return map;
    } catch (IOException e) {
      return null;
    }
  }

  public static String[] getParameterNames(Constructor<?> constructor) {
    var c = constructor.getDeclaringClass();
    var map = parameterNamesCache.get(c);
    if (map == null) {
      map = inspectClass(c);
      if (map == null) {
        map = NOT_FOUND;
      }
      parameterNamesCache.put(c, map);
    }
    if (map != NOT_FOUND) {
      return map.get(constructor);
    }
    return null;
  }


  private static class ParameterNameDiscoveringVisitor extends ClassVisitor {

    private static final String STATIC_CLASS_INIT = "<clinit>";

    private final Class<?> clazz;

    private final Map<Member, String[]> memberMap;

    public ParameterNameDiscoveringVisitor(Class<?> clazz, Map<Member, String[]> memberMap) {
      super(Opcodes.ASM9);
      this.clazz = clazz;
      this.memberMap = memberMap;
    }

    private static boolean isSyntheticOrBridged(int access) {
      return (((access & Opcodes.ACC_SYNTHETIC) | (access & Opcodes.ACC_BRIDGE)) > 0);
    }

    private static boolean isStatic(int access) {
      return ((access & Opcodes.ACC_STATIC) > 0);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      if (!isSyntheticOrBridged(access) && !STATIC_CLASS_INIT.equals(name)) {
        return new LocalVariableTableVisitor(clazz, memberMap, name, desc, isStatic(access));
      }
      return null;
    }
  }

  private static class LocalVariableTableVisitor extends MethodVisitor {

    private static final String CONSTRUCTOR = "<init>";

    private final Class<?> clazz;

    private final Map<Member, String[]> memberMap;

    private final String name;

    private final org.objectweb.asm.Type[] args;

    private final String[] parameterNames;

    private final boolean isStatic;

    private final int[] lvtSlotIndex;
    private boolean hasLvtInfo = false;

    public LocalVariableTableVisitor(Class<?> clazz, Map<Member, String[]> map, String name, String desc, boolean isStatic) {
      super(Opcodes.ASM9);
      this.clazz = clazz;
      this.memberMap = map;
      this.name = name;
      this.args = org.objectweb.asm.Type.getArgumentTypes(desc);
      this.parameterNames = new String[this.args.length];
      this.isStatic = isStatic;
      this.lvtSlotIndex = computeLvtSlotIndices(isStatic, this.args);
    }

    private static int[] computeLvtSlotIndices(boolean isStatic, org.objectweb.asm.Type[] paramTypes) {
      int[] lvtIndex = new int[paramTypes.length];
      int nextIndex = (isStatic ? 0 : 1);
      for (int i = 0; i < paramTypes.length; i++) {
        lvtIndex[i] = nextIndex;
        if (isWideType(paramTypes[i])) {
          nextIndex += 2;
        } else {
          nextIndex++;
        }
      }
      return lvtIndex;
    }

    private static boolean isWideType(org.objectweb.asm.Type aType) {
      return (aType == org.objectweb.asm.Type.LONG_TYPE || aType == org.objectweb.asm.Type.DOUBLE_TYPE);
    }

    @Override
    public void visitLocalVariable(String name, String description, String signature, Label start, Label end, int index) {
      this.hasLvtInfo = true;
      for (int i = 0; i < this.lvtSlotIndex.length; i++) {
        if (this.lvtSlotIndex[i] == index) {
          this.parameterNames[i] = name;
        }
      }
    }

    @Override
    public void visitEnd() {
      if (this.hasLvtInfo || (this.isStatic && this.parameterNames.length == 0)) {
        this.memberMap.put(resolveMember(), this.parameterNames);
      }
    }

    private Member resolveMember() {
      ClassLoader loader = this.clazz.getClassLoader();
      Class<?>[] argTypes = new Class<?>[this.args.length];
      for (int i = 0; i < this.args.length; i++) {
        argTypes[i] = ClassUtils.resolveClassName(this.args[i].getClassName(), loader);
      }
      try {
        if (CONSTRUCTOR.equals(this.name)) {
          return this.clazz.getDeclaredConstructor(argTypes);
        }
        return this.clazz.getDeclaredMethod(this.name, argTypes);
      } catch (NoSuchMethodException ex) {
        throw new IllegalStateException("Method [" + this.name +
            "] was discovered in the .class file but cannot be resolved in the class object", ex);
      }
    }
  }


}
