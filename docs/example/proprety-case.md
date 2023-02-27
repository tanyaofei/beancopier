# Property Cases

`@Property` is an annotation to mark on field or record component.

## Case 1 - alias

There are two ways to define an alias for a property.

Here is a class that will be used as copying source.

```java
public class User {
  private String userId;
}
```

### define an alias via value

```java
public class UserDTO {
  @Property(value = "userId")
  private String id;
}
```

When using `value` to define an alias,the value of `value` will be used to find the corresponding
property.

### define aliases via @Alias

```java
public class UserDTO {
  @Property(alias = {@Alias(value = "userId", forType = {User.class})})
  private String id;
}
```

When using `@Alias` to define aliases, `@Alias.value` will only be used as an alias when `@Alias.forType` includes the
class of Source. It's not recommended to define the same class in multiple `@Alias.forType`, if you did so, the first
matching `@Alias` will be used.

If a `@Property` has both `value` and `@Alias`, beancopier will first try to find a matching alias from `@Alias`,
and if none is found, it will use `value`.

## Case 2 - skip

You can use `@Property(skip = true)` to tell beancopier that do not copy properties.

```java
public class UserDTO {
  @Property(skip = true)
  private String id = "always me";
}
```

But, it won't take effect on copying to a record. In the following case, the `name` of `User` will still be copied to
the `name` of `UserDTO`.

```java
public record UserDTO(
    @Property(skip = true) String name
) {
}
```