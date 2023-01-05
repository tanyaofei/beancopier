# 该项目为对象拷贝工具

因为 cglib 的 BeanCopier 不支持 lombok 的链式 setter（`@Accessors(chain=true)`）， 因此编写了此工具，该工具使用 ASM
技术在运行时生成转换器(
实现 Convert 接口)，性能等同于 `get/set` 拷贝

1. 本文的所有拷贝来源的 class 都使用 `Source` 指代
2. 本文的所有拷贝结果的 class 都是用 `Target` 指代
3. 本拷贝工具拷贝均为`浅拷贝`

# 使用方式
```xml
<dependency>
   <groupId>io.github.tanyaofei</groupId>
   <artifactId>beancopier</artifactId>
   <version>0.1.0</version>
</dependency>
```

## 性能对比

| 拷贝工具             | 拷贝 1 百万个对象耗时 | 递归拷贝 | 链式 setter 对象支持 |
|------------------|--------------|------|----------------|
| **BeanCopier**   | 141ms        | ✔️   | ✔️             |
| cglib BeanCopier | 79ms         | ❌    | ❌              |
| BeanUtils        | 5832ms       | ❌    | ✔️             |
| ModelMapper      | 2611ms       | ✔️   | ✔️             |

## 使用前提和约束

1. `Source` 和 `Target` 必须为 public，否则检验失败无法进行拷贝
2. `Source` 需要提供相应的 public `getter` ，`Target` 需要提供相应的 public `setter`
3. `Target` 需要提供一个 `无参构造函数`
4. `Source` 中与 `Target` 同名并且类型兼容字段才会被拷贝（递归拷贝除外）, 如 `List<String>` 是不会被拷贝到 `List<Integer>`
   的,
   简单理解凡是可以直接编写代码进行 `get/set` 的都可以, 其他都不行(除了递归拷贝情况)
5. 递归拷贝约束:
   + `Source` 的某字段为 `Source` 类型或 `List<Source>`
   + `Target` 的某字段为 `Target` 类型或 `List<Target>`
   + 这两个字段名称一样

## 功能完成清单

1. [X] 普通字段的拷贝

2. [X] 递归字段的拷贝

3. [X] 列表递归字段的拷贝

4. [X] 批量拷贝

5. [X] 批量拷贝回调动作

6. [X] 无范型向上转型拷贝(如 `Integer` 拷贝为 `Number`)

7. [X] 有范型的向上转型拷贝(如 `ArrayList<Integer>` 拷贝为 `List<Integer>`, 但是范型必须一致)

8. [X] 拷贝父类字段

9. [X] 字段别名

10. [X] 跳过字段

## 简单例子

```java

@Data
@Accessors(chain = true)
public static class Source {

  private String a;
  private Integer b;
  private LocalDateTime c;
  private Source d;
  private List<Source> e;
  private List<String> f;
  private ArrayList<Integer> g;
  private InnerField h;
  private Integer i;
  private List<Integer> j;
}

@Data
@Accessors(chain = true)
public static class Target {

  private String a;         // ok
  private String b;         // null, 类型不匹配
  private LocalDateTime c;  // ok
  private Target d;         // ok, 递归拷贝
  private List<Target> e;   // ok, 列表递归拷贝
  private List<String> f;   // ok
  private List<Integer> g;  // ok, 带范型向上转型
  private InnerField h;     // ok
  private Number i;         // ok, 向上转型
  private List<Number> j;   // null, 范型类型不一致
  private Object z;         // null, 不存在同名字段
}
```

```java
public class Main {
   public static void main(String[] args) {
      // 单个拷贝
      Source source = new Source();
      Target target = BeanCopier.copy(source, Target.class);

      // 列表拷贝
      List<Source> sources=List.of(new Source(),new Source());
      List<Target> targets=BeanCopier.copyList(sources,Target.class);

      // 列表拷贝后回调动作, 可以用来手动实现一些无法拷贝的字段或其他复杂动作
      List<Target> targets=BeanCopier.copyList(
              sources,
              Target.class,
              (source,target)->target.setF(target.getA())
      );
   }
}
```

## 字段别名

使用 `@Property(value = "xxx")` 为字段指定别名
<p><b>当使用别名时, 在拷贝时不再拷贝同字段名称的同类型字段, 而是拷贝字段名称为别名的同类型字段</b></p>

```java
import io.github.tanyaofei.beancopier.annotation.Property;
import lombok.experimental.Accessors;
import lombok.Data;

public class Source {
   private String value;
}

public class Target {
   @Property("value")   // 从 Source 拷贝时使用 value 字段
   private String val;
}
```

## 跳过/不拷贝字段
使用 `@Property(skip=true)` 表示该字段不需要拷贝

```java
import io.github.tanyaofei.beancopier.annotation.Property;
import lombok.experimental.Accessors;
import lombok.Data;

@Data
@Accessors(chain = true)
public class Source {
   private String value1;
   private String value2;
}

@Data
@Accessors(chain = true)
public class Target {
   private String value1;
   @Property(skip = true) // 拷贝时跳过此字段, 因此为 null
   private String value2;
}
```


## 调试

通过设置启动参数可以将生成出来的字节码文件写入到磁盘便于调试

```java
public class Main {
   public static void main(String[] args) {
      System.setProperty(BeanCopierConfiguration.PropertyNames.CONVERTER_CLASS_DUMP_PATH,"./");
   }
}
```

## 原理

该工具原理是在运行时反射获取 `Source` 和 `Target` 的字段信息，使用 ASM 技术动态生成字节码并加载到自定义类加载器中，对于以上的例子可参考生成出来的
class 文件内容为以下

```java
public class SourceToTargetConverter$$GeneratedByBeanCopier$$7b8a009 implements Converter<Source, Target> {
   public Target convert(Source var1) {
    Target var2 = new Target();
    var2.setA(var1.getA());
    var2.setC(var1.getC());
    if (var1.getD() != null) {
      var2.setD(this.convert(var1.getD()));
    }

    if (var1.getE() != null) {
      var2.setE((List) var1.getE().stream().map(this::convert).collect(Collectors.toList()));
    }

    var2.setF(var1.getF());
    var2.setG(var1.getG());
    var2.setH(var1.getH());
    var2.setI(var1.getI());
    return var2;
  }
}
```
