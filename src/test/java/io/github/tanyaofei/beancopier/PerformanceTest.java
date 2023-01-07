package io.github.tanyaofei.beancopier;

import com.google.common.base.Stopwatch;
import jdk.internal.org.objectweb.asm.Type;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTimeout;

public class PerformanceTest {

  private final static Duration TIMEOUT = Duration.ofMillis(500);
  private final static int N = 1_000_000;

  static {
    System.setProperty(BeanCopierConfiguration.PropertyNames.CONVERTER_CLASS_DUMP_PATH, "./target");
  }

  private static List<Obj> objects(int n) {
    Stopwatch stopwatch = Stopwatch.createUnstarted();
    ArrayList<Obj> objs = new ArrayList<>(n);

    stopwatch.start();
    for (int i = 0; i < n; i++) {
      objs.add(new Obj().setA("1").setB("1").setC("1").setD("1").setE("1").setF("1").setG("1").setH("1").setI("1"));
    }
    stopwatch.stop();
    System.out.println("Time of creating " + n + " objects: " + stopwatch.elapsed().toMillis() + " ms");
    stopwatch.reset();
    return objs;
  }

  @Test
  public void testClone() {
    List<Obj> sources = objects(N);
    BeanCopier.clone(sources.get(0));
    Stopwatch stopwatch = Stopwatch.createUnstarted();

    stopwatch.start();
    assertTimeout(TIMEOUT, () -> {
      for (int i = 0; i < N; i++) {
        BeanCopier.clone(sources.get(i));
      }
    });
    stopwatch.stop();
    System.out.println("Time of copying " + N + " objects: " + stopwatch.elapsed().toMillis() + " ms");
  }

  @Test
  public void testCopy() {
    List<Obj> sources = objects(N);
    BeanCopier.copy(sources.get(0), Obj.class);
    Stopwatch stopwatch = Stopwatch.createUnstarted();

    stopwatch.start();
    assertTimeout(TIMEOUT, () -> {
      for (int i = 0; i < N; i++) {
        BeanCopier.copy(sources.get(i), Obj.class);
      }
    });
    stopwatch.stop();
    System.out.println("Time of copying " + N + " objects: " + stopwatch.elapsed().toMillis() + " ms");
  }

  @Test
  public void testCloneList() {
    List<Obj> sources = objects(N);
    BeanCopier.clone(sources.get(0));
    Stopwatch stopwatch = Stopwatch.createUnstarted();

    stopwatch.start();
    BeanCopier.cloneList(sources);
    stopwatch.stop();
    System.out.println("Time of copying " + N + " objects: " + stopwatch.elapsed().toMillis() + " ms");
  }

  @Test
  public void testCopyList() {
    List<Obj> sources = objects(N);
    BeanCopier.copy(sources.get(0), Obj.class);
    Stopwatch stopwatch = Stopwatch.createUnstarted();

    stopwatch.start();
    BeanCopier.copyList(sources, Obj.class);
    stopwatch.stop();
    System.out.println("Time of copying " + N + " objects: " + stopwatch.elapsed().toMillis() + " ms");
  }

  @Test
  public void testCglib() {
    List<Obj> sources = objects(N);
    net.sf.cglib.beans.BeanCopier beanCopier = net.sf.cglib.beans.BeanCopier.create(Obj.class, Obj.class, false);
    Stopwatch stopwatch = Stopwatch.createUnstarted();

    stopwatch.start();
    ArrayList<Obj> targets = new ArrayList<>(N);
    for (Obj source : sources) {
      Obj target = new Obj();
      beanCopier.copy(source, target, null);
      targets.add(target);
    }
    stopwatch.stop();
    System.out.println("Time of copying " + N + " objects: " + stopwatch.elapsed().toMillis() + " ms");
  }

  @Test
  public void testBeanUtils() throws InvocationTargetException, IllegalAccessException {
    List<Obj> sources = objects(N);
    Stopwatch stopwatch = Stopwatch.createUnstarted();

    stopwatch.start();
    ArrayList<Obj> targets = new ArrayList<>(N);
    for (Obj source : sources) {
      Obj target = new Obj();
      BeanUtils.copyProperties(source, target);
      targets.add(target);
    }
    stopwatch.stop();
    System.out.println("Time of copying " + N + " objects: " + stopwatch.elapsed().toMillis() + " ms");
  }

  @Test
  public void testModelMapper() {
    List<Obj> sources = objects(N);
    ModelMapper modelMapper = new ModelMapper();
    Stopwatch stopwatch = Stopwatch.createUnstarted();

    stopwatch.start();
    ArrayList<Obj> targets = new ArrayList<>(N);
    for (Obj source : sources) {
      Obj target = new Obj();
      modelMapper.map(source, target);
      targets.add(target);
    }
    stopwatch.stop();
    System.out.println("Time of copying " + N + " objects: " + stopwatch.elapsed().toMillis() + " ms");
  }


  @Data
  @Accessors(chain = true)
  public static class Obj {
    private String a;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;
    private String g;
    private String h;
    private String i;
  }

  @Test
  public void testGenerateConverter() throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    XClassLoader classloader = new XClassLoader();
    List<Class<?>> classes = new LinkedList<>();

    // 通过重命名 TemplateObject 来创建不同的类
    for (int i = 0; i < 100; i++) {
      ClassReader cr = new ClassReader(TemplateObject.class.getName());
      ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

      Remapper remapper = new SimpleRemapper(Type.getInternalName(TemplateObject.class), Type.getInternalName(TemplateObject.class) + i);
      ClassVisitor cv = new ClassRemapper(cw, remapper);

      cr.accept(cv, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
      byte[] code = cw.toByteArray();
      classes.add(classloader.defineClass(code));
    }

    ConverterFactory converterFactory = new ConverterFactory(new ConverterClassLoader(this.getClass().getClassLoader()), NamingPolicy.getDefault(), null);
    TemplateObject source = new TemplateObject();

    Stopwatch stopwatch = Stopwatch.createUnstarted();
    stopwatch.start();
    for (Class<?> c : classes) {
      converterFactory.generateConverter(source.getClass(), c);
    }
    stopwatch.stop();
    System.out.println("Time of creating " + classes.size() + " converter classes: " + stopwatch.elapsed().toMillis() + " ms");
  }


  public static class XClassLoader extends ClassLoader {
    public Class<?> defineClass(byte[] code) {
      return super.defineClass(null, code, 0, code.length);
    }
  }

}
