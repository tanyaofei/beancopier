# beancopier: High performance bean copying tool.



## Getting started

```xml
<dependency>
    <!-- https://mvnrepository.com/artifact/io.github.tanyaofei/beancopier -->
    <groupId>io.github.tanyaofei</groupId>
    <artifactId>beancopier</artifactId>
    <version>The version of beancopier</version>
</dependency>
```

_Starting from version 0.2.x, the minimun required JDK version is 17. If you are using a lower version, please use version 0.1.x._


## What can this tool provide:

1. Support copying fields of the same type
2. Support copying fields of compatible types: `Integer -> Number`, `List<Integer> -> Collection<? extends Number>`
3. Support nested copying, collection nested copying
4. Support copying fields from parent classes
5. Support setting field aliases
6. Support ignoring the copying of certain fields
7. Support JDK 16 record：`POJO -> record`, `record -> record`, `record -> POJO`



## Performance comparison

| Copying Tool     | The time it takes to copy one million objects |
| ---------------- | --------------------------------------------- |
| **beancopier**   | **27ms**                                      |
| cglib BeanCopier | 20ms                                          |
| BeanUtils        | 1387ms                                        |
| ModelMapper      | 4262ms                                        |



## Hot to use it

### Source

```java
class Menu {
    private Long id;
    private String name;
    private Menu parent;
    private List<Menu> sub;
}
```

### Target

```java
record MenuDTO(
	Number id,
    String name,
    MenuDTO parent,
    Collection<MenuDTO> sub,
    long createdAtTimestamp,
    LocalDateTime lastModifiedAt
){}
```

```java
public class Example {
  public static void main(String[] args) {
    MenuDTO menuDTO = BeanCopier.copy(menu, MenuDTO.class);
    assertEquals(menu.getId(), menuDTO.id());
    assertEquals(menu.getName(), menuDTO.name());
    assertNotNull(menuDTO.parent());
    assertNoNull(menuDTO.sub());
    assertEquals(menuDTO.layer(), 0);
  }
}
```



### BeanCopier Configuration

```java
public class Example {
  public static void main(String[] args) {
    BeanCopierImpl beanCopier = new BeanCopierImpl(config ->
            config
                    .preferNested(true)
                    .includingSuper(true)
                    .skipNull(false)
                    .propertySupported(true)
                    .classLoader(ClassLoader.getSystemClassLoader())
                    .namingPolicy(NamingPolicy.getDefault())
                    .fullTypeMatching(false)
                    .classDumpPath("./target")
    );
  }
}
```



## Debugging

To export the generated bytecode to disk, you can set the runtime parameter.

```java
public class Example {
   public static void main(String[] args) {
      System.setProperty(BeanCopierConfiguration.PropertyNames.CONVERTER_CLASS_DUMP_PATH, "./");
   }
}
```



## Principle

Analyze the field relationships between two classes and use ASM to dynamically generate bytecode at runtime.

### Some decompiled source code of generated class files

```java
package io.github.tanyaofei.beancopier.converter;

import io.github.tanyaofei.beancopier.converter.Converter;
import io.github.tanyaofei.beancopier.test.simple.SimplePOJO;
import java.time.LocalDateTime;

// $FF: synthetic class
public class SimpleObjectToSimpleObjectConverter$$GeneratedByBeanCopier$$5de41a00 implements Converter<SimpleObject, SimpleObject> {
  public SimpleObject convert(SimpleObject var1) {
    if (var1 == null) {
      return null;
    } else {
      Boolean var2 = var1.getBooleanVal();
      Byte var3 = var1.getByteVal();
      Short var4 = var1.getShortVal();
      Integer var5 = var1.getIntVal();
      Long var6 = var1.getLongVal();
      Float var7 = var1.getFloatVal();
      Double var8 = var1.getDoubleVal();
      Character var9 = var1.getCharVal();
      String var10 = var1.getStringVal();
      LocalDateTime var11 = var1.getLocalDateTimeVal();
      SimpleObject var12 = new SimpleObject();
      var12.setBooleanVal(var2);
      var12.setByteVal(var3);
      var12.setShortVal(var4);
      var12.setIntVal(var5);
      var12.setLongVal(var6);
      var12.setFloatVal(var7);
      var12.setDoubleVal(var8);
      var12.setCharVal(var9);
      var12.setStringVal(var10);
      var12.setLocalDateTimeVal(var11);
      return var12;
    }
  }
}
```

```java
package io.github.tanyaofei.beancopier.converter;

import io.github.tanyaofei.beancopier.converter.Converter;
import io.github.tanyaofei.beancopier.test.nested.NestedRecord;
import java.util.List;

// $FF: synthetic class
public class NestedRecordToNestedRecordConverter$$GeneratedByBeanCopier$$6888e1c0 implements Converter<NestedRecord, NestedRecord> {
  public NestedRecord convert(NestedRecord var1) {
    if (var1 == null) {
      return null;
    } else {
      int var2 = var1.seniority();
      NestedRecord var3 = var1.child();
      List var4 = var1.children();
      NestedRecord var5 = new NestedRecord(var2, var3, var4);
      return var5;
    }
  }
}
```