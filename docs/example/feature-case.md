# Feature Cases
You can define whether to enable or disable certain features when creating `BeanCopierImpl`.

```java
public class Example {
  public static void main(String[] args) {
    BeanCopierImpl beanCopier = new BeanCopierImpl(
        feature -> feature
            .preferNested(true)
            .includingSuper(true)
            .skipNull(false)
            .propertySupported(true)
            .lookup(LookupUtils.lookupInModule(classloader, MyClassLoader::defineClass))
            .namingPolicy(NamingPolicy.getDefault())
            .fullTypeMatching(false)
            .debugLocation("./target"));
  }
}
```
