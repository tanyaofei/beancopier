# Simple Cases

## Case 1 - properties with the same name and type

```java

@lombok.Data
@lombok.experimental.Accessors(chain = true)
public class User {
  private String name;
  private Long age;
  private List<String> roleNames;
}
```

```java

@lombok.Data
@lombok.experimental.Accessors(chain = true)
public class UserDTO {
  private String name;
  private Long age;
  private List<String> roleNames;
}
```

In this case, the following properties will be copied

+ `User.name -> UserDTO.name`
+ `User.age -> User.age`
+ `User.roleNames -> User.roleNames`

## Case 2 - properties with the same name and compatible types

```java
@lombok.Data
@lombok.experimental.Accessors(chain = true)
public class User {
  private String name;
  private Long age;
  private ArrayList<String> roleNames;
}
```

```java
@lombok.Data
@lombok.experimental.Accessors(chain = true)
public class UserDTO {
  private Character name;
  private Number age;
  private List<? extends Character> roleNames;
}
```

In this case, the following properties will be copied

+ `User.name -> UserDTO.name`
+ `User.age -> User.age`
+ `User.roleNames -> User.roleNames`
