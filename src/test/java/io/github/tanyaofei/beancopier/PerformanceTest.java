package io.github.tanyaofei.beancopier;

import com.google.common.base.Stopwatch;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.ArrayList;
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

}
