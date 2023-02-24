package io.github.tanyaofei.beancopier.test;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.LookupUtils;
import io.github.tanyaofei.beancopier.core.ConverterFactory;
import io.github.tanyaofei.beancopier.test.util.TemplateObject;
import io.github.tanyaofei.guava.common.base.Stopwatch;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PerformanceTest extends Assertions {

  private static List<Obj> objs;

  @BeforeAll
  public static void createObjs() {
    int n = 1_000_000;
    objs = new ArrayList<>(n);
    Stopwatch stopwatch = Stopwatch.createStarted();
    for (int i = 0; i < n; i++) {
      objs.add(new Obj().setA("1").setB("1").setC("1").setD("1").setE("1").setF("1").setG("1").setH("1").setI("1"));
    }
    System.out.println("-------- createObjs() -------");
    System.out.println("time: " + stopwatch.elapsed().toMillis() + " ms");
    System.out.println("-----------------------------\n");
  }

  @Test
  public void testClone() {
    BeanCopier.clone(objs.get(0));
    Stopwatch stopwatch = Stopwatch.createUnstarted();

    stopwatch.start();
    for (Obj obj : objs) {
      BeanCopier.clone(obj);
    }
    stopwatch.stop();

    System.out.println("-------- testClone() --------");
    System.out.println("time: " + stopwatch.elapsed().toMillis() + " ms");
    System.out.println("-----------------------------");
  }

  @Test
  public void testCopy() {
    BeanCopier.copy(objs.get(0), Obj.class);
    Stopwatch stopwatch = Stopwatch.createUnstarted();

    stopwatch.start();
    for (Obj obj : objs) {
      BeanCopier.copy(obj, Obj.class);
    }
    stopwatch.stop();

    System.out.println("-------- testCopy() --------");
    System.out.println("time: " + stopwatch.elapsed().toMillis() + " ms");
    System.out.println("-----------------------------");
  }

  @Test
  public void testCloneList() {
    BeanCopier.clone(objs.get(0));
    Stopwatch stopwatch = Stopwatch.createUnstarted();

    stopwatch.start();
    BeanCopier.cloneList(objs);
    stopwatch.stop();

    System.out.println("-------- testCloneList() --------");
    System.out.println("time: " + stopwatch.elapsed().toMillis() + " ms");
    System.out.println("---------------------------------");
  }

  @Test
  public void testCopyList() {
    BeanCopier.copy(objs.get(0), Obj.class);
    Stopwatch stopwatch = Stopwatch.createUnstarted();

    stopwatch.start();
    BeanCopier.copyList(objs, Obj.class);
    stopwatch.stop();

    System.out.println("-------- testCopyList() --------");
    System.out.println("time: " + stopwatch.elapsed().toMillis() + " ms");
    System.out.println("--------------------------------");
  }

  @Test
  public void testCglib() {
//    net.sf.cglib.beans.BeanCopier beanCopier = net.sf.cglib.beans.BeanCopier.create(Obj.class, Obj.class, false);
//    Stopwatch stopwatch = Stopwatch.createUnstarted();
//
//    stopwatch.start();
//    ArrayList<Obj> targets = new ArrayList<>(objs.size());
//    for (Obj o : objs) {
//      Obj target = new Obj();
//      beanCopier.copy(o, target, null);
//      targets.add(target);
//    }
//    stopwatch.stop();
//    System.out.println("-------- testCglib() --------");
//    System.out.println("time: " + stopwatch.elapsed().toMillis() + " ms");
//    System.out.println("-----------------------------");
  }

  @Test
  public void testBeanUtils() throws InvocationTargetException, IllegalAccessException {
    Stopwatch stopwatch = Stopwatch.createUnstarted();

    stopwatch.start();
    ArrayList<Obj> targets = new ArrayList<>(objs.size());
    for (Obj o : objs) {
      Obj target = new Obj();
      BeanUtils.copyProperties(o, target);
      targets.add(target);
    }
    stopwatch.stop();

    System.out.println("-------- testCopy() --------");
    System.out.println("time: " + stopwatch.elapsed().toMillis() + " ms");
    System.out.println("-----------------------------");
  }

  @Test
  public void testModelMapper() {
    ModelMapper modelMapper = new ModelMapper();
    Stopwatch stopwatch = Stopwatch.createUnstarted();

    stopwatch.start();
    ArrayList<Obj> targets = new ArrayList<>(objs.size());
    for (Obj source : objs) {
      Obj target = new Obj();
      modelMapper.map(source, target);
      targets.add(target);
    }
    stopwatch.stop();
    System.out.println("-------- testModelMapper() --------");
    System.out.println("time: " + stopwatch.elapsed().toMillis() + " ms");
    System.out.println("-----------------------------------");
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
  public void testGenerateConverter() throws IOException {
    var classloader = new XClassLoader();
    List<Class<?>> classes = new LinkedList<>();

    // 通过重命名 TemplateObject 来创建不同的类
    for (int i = 0; i < 10000; i++) {
      var cr = new ClassReader(TemplateObject.class.getName());
      var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

      var remapper = new SimpleRemapper(Type.getInternalName(TemplateObject.class), Type.getInternalName(TemplateObject.class) + i);
      var cv = new ClassRemapper(cw, remapper);

      cr.accept(cv, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
      var code = cw.toByteArray();
      classes.add(classloader.defineClass(code));
    }

    var converterFactory = new ConverterFactory(
        feature -> feature
            .lookup(LookupUtils.lookupInModule(classloader, XClassLoader::defineClass))
            .debugLocation(null)
    );
    var o = new TemplateObject();

    var stopwatch = Stopwatch.createStarted();
    for (Class<?> c : classes) {
      converterFactory.genConverter(o.getClass(), c);
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
