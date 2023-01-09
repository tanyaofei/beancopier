# 该项目为对象拷贝工具

因为 cglib 的 BeanCopier 不支持 lombok 的链式 setter（`@Accessors(chain=true)`）， 因此编写了此工具，该工具使用 ASM
技术在运行时生成转换器(
实现 Convert 接口)，性能等同于 `get/set` 拷贝

# 使用方式
```xml
<dependency>
   <groupId>io.github.tanyaofei</groupId>
   <artifactId>beancopier</artifactId>
   <version>0.1.4</version>
</dependency>
```

## 性能对比

| 拷贝工具             | 拷贝 1 百万个对象耗时 | 递归拷贝 | 链式 setter 对象支持 |
|------------------|--------------|------|----------------|
| **BeanCopier**   | 17ms         | ✔️   | ✔️             |
| cglib BeanCopier | 20ms         | ❌    | ❌              |
| BeanUtils        | 1387ms       | ❌    | ✔️             |
| ModelMapper      | 4262ms       | ✔️   | ✔️             |

## 使用前提和约束

1. `Source` 和 `Target` 必须为 public，否则检验失败无法进行拷贝
2. `Source` 需要提供相应的 public `getter`(如果是 `boolean` 类型则为 `isXxx()`) ，`Target` 需要提供相应的
   public `setter`, 不关心 `setter` 是否具有返回值
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

7. [X] **完全兼容**的有范型的向上转型拷贝(如 `ArrayList<Integer>` 拷贝为 `List<Integer>`)

8. [X] 拷贝父类字段

9. [X] 字段别名

10. [X] 跳过字段

## 使用

### 简单使用

```java
import io.github.tanyaofei.beancopier.BeanCopier;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class A {
   private String a;
   private Integer b;
   private Long c;
   private String d;
}

@Data
@Accessors(chain = true)
public class B {
   private String a;
   private Integer b;
   private Long c;
   private String d;
}

public class Main {

   public static void main(String[] args) {
      A a = new A();
      // ... set fields
      B b = BeanCopier.copy(a, B.class);
      assertEquals(a.getA(), b.getA());
      assertEquals(a.getB(), b.getB());
      assertEquals(a.getC(), b.getC());
      assertEquals(a.getD(), b.getD());
   }

}
```

### 递归拷贝

```java
import io.github.tanyaofei.beancopier.BeanCopier;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class Node {
   private Node child;
   private List<Node> children;
   private String string;
}

@Data
@Accessors(chain = true)
public class Node2 {
   private Node2 child;
   private List<Node2> children;
   private String string;
}

public class Main {

   public static void main(String[] args) {
      Node n1 = new Node();
      // ... set fields
      Node2 n2 = BeanCopier.copy(n1, Node2.class);
      assertEquals(n1.getChild().getString(), n2.getChild().getString());
      for (int i = 0; i < n1.getChildren().size(); i++) {
         assertEquals(n1.getChildren().get(i).getString(), n2.getChildren().get(i).getString());
      }
   }

}
```

### 向上转型拷贝

在这个例子中

+ `Integer` -> `Number`
+ `ArrayList<String>` -> `List<String>`
+ `List<Integer>` -> `List<? extends Number>`

```java
import io.github.tanyaofei.beancopier.BeanCopier;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;

@Data
@Accessors(chain = true)
public class A {
   private Integer a;
   private ArrayList<String> b;
   private List<Integer> c;
}

@Data
@Accessors(chain = true)
public class B {
   private Number a;
   private List<String> b;
   private List<? extends Number> c;
}

public class Main {
   public static void main(String[] args) {
      A a = new A();
      // ... set fields
      B b = BeanCopier.copy(a, B.class);
      assertEquals(a.getA(), b.getA());
      assertEquals(a.getB(), b.getB());
      assertEquals(a.getC(), b.getC());
   }
}
```

### 继承字段拷贝

```java
import io.github.tanyaofei.beancopier.BeanCopier;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class Parent {
   private String a;
}

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class Child extends Parent {
   private String b;
}

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class Child2 extends Parent {
   private String b;
}

public class Main {
   public static void main(String[] args) {
      Child child = new Child();
      // ... set fields
      Child2 child2 = BeanCopier.copy(child, Child2.class);
      assertEquals(child.getA(), child2.getA());
      assertEquals(child.getB(), child2.getB());
   }
}
```

### 字段别名

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

### 跳过/不拷贝字段
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

## 版本记录
+ 0.1.4
  + 支持通过 `new BeanCopier(new MyClassLoader())` 创建指定类加载器的 `BeanCopierImpl`
  + `BeanCopier` 的类加载器由原来的 `ConverterClassLoader` 替换为 `AppClassLoader`
  + 修复列表递归拷贝元素包含 null 时会抛出异常的问题
  + 优化拷贝效率并减少内存占用
  + asm 依赖库升级到 `9.4`

+ 0.1.3
  + 大幅度优化批量拷贝的速度 `BeanCopier.cloneList()` 和 `BeanCopier.copyList()`, 拷贝一百万个对象由 `200ms+` 缩减到 `20ms+`
  + 修复 `BeanCopier.cloneList` 第一个元素为 null 时会出现 `NullPointerException` 的 bug

+ 0.1.2
  + 除了提供 `BeanCopier` 的静态方法以外，现在可 `new BeanCopierImpl()` 来创建拷贝对象，适用于有类卸载需求的场景
  + 更多测试用例

+ 0.1.1
  + 修正一些 bug
  + 完全的泛型兼容, `List<Integer>` 现在可以拷贝到 `List<? extends Number>`, `IntegerBox extends Box<Integer>` 现在可以拷贝到 `Box<Integer>`
 
+ 0.1.0
  + 新增 `@Property` 注解支持字段别名和跳过字段选项

+ 0.0.8
  + lombok 依赖 `scope` 修改为 `provided`

+ 0.0.7
  + 不拷贝提供了参数数量不正确的 `getter` 和 `setter`

+ 0.0.5
  + 基础类型 `boolean` 的 `getter` 方法修改为 `isXxxx()`


## 调试

通过设置启动参数可以将生成出来的字节码文件写入到磁盘便于调试

```java
public class Main {
   public static void main(String[] args) {
      System.setProperty(BeanCopierConfiguration.PropertyNames.CONVERTER_CLASS_DUMP_PATH, "./");
   }
}
```

## 原理

该工具原理是在运行时反射获取两个对象的字段信息, 并且根据字段信息使用 ASM 生成对应的转换器并缓存起来, 之后都使用该转换器进行
get/set 拷贝

### 一些生成出来的 class 文件反编译后的源码

```java
public class StandardSourToStandardDestConverter$$GeneratedByBeanCopier$$ba69502d implements Converter<NormalTest.StandardSour, NormalTest.StandardDest> {
   public NormalTest.StandardDest convert(NormalTest.StandardSour var1) {
      NormalTest.StandardDest var2 = new NormalTest.StandardDest();
      var2.setA(var1.getA());
      var2.setB(var1.getB());
      var2.setC(var1.getC());
      var2.setD(var1.getD());
      var2.setE(var1.getE());
      var2.setF(var1.getF());
      var2.setG(var1.getG());
      var2.setH(var1.getH());
      var2.setI(var1.getI());
      var2.setJ(var1.getJ());
      var2.setK(var1.getK());
      var2.setL(var1.getL());
      return var2;
   }
}
```

```java
import java.util.List;

public class RecursionSourToRecursionDestConverter$$GeneratedByBeanCopier$$41d66bc9 implements Converter<NormalTest.RecursionSour, NormalTest.RecursionDest> {
  public NormalTest.RecursionDest convert(NormalTest.RecursionSour var1) {
    NormalTest.RecursionDest var2 = new NormalTest.RecursionDest();
    if (var1.getA() != null) {
      var2.setA(this.convert(var1.getA()));
    }

    if (var1.getB() != null) {
      var2.setB((List)var1.getB().stream().map((var1x) -> {
        return var1x == null ? null : this.convert(var1x);
      }).collect(Collectors.toList()));
    }

    var2.setC(var1.getC());
    var2.setD(var1.getD());
    return var2;
  }
}
```

## 类卸载
由于该工具会在运行时生成类, 开发者可斟酌是否需要进行类卸载来减少长期的内存占用. 如果需要类卸载能力的话应当避免直接使用 `BeanCopier` 提供的静态方法, 而是通过 `new BeanCopierImpl()` 来使用.
当 `BeanCopierImpl` 实例被释放时, 使用该实例生成的转换器对象, 转换器类都会被 GC 掉.

```java
import io.github.tanyaofei.beancopier.BeanCopierImpl;

public class Main {
   public static void main(String[] args) {
      BeanCopierImpl beanCopier = new BeanCopierImpl();
      beanCopier.copy(new Object(), new Object());
      
      beanCopier = null;
      System.gc();
      // ... 
      // 在 GC 之后, Object -> Object 的转换器实例和类将会被清理掉
   }
}
```