# Nested Cases

## Case 1: nested copy

```java
public class Node {
  private String name;
  private Node next;
}
```

```java
public class NodeDTO {
  private String name;
  private NodeDTO next;
}
```

In this case, the following properties will be copied:

+ `Node.name -> NodeDTO.name`

And the following properties will be copied after conversion:

+ `Node.next -> Node.next`

## Case 2: iterable nested copy

```java
public class Menu {
  private String name;
  private List<Menu> sub;
}
```

```java
public class MenuDTO {
  private String name;
  private Collection<MenuDTO> sub;
}
```

In this case, the following properties will be copied:

+ `Menu.name -> MenuDTO.name`

And the following properties will be copied after conversion:

+ `Menu.sub -> MenuDTO.sub`

The generated Converter will look like this:

## Case 3: array nested copy

```java
public class Menu {
  private String name;
  private List<Menu> sub;
}
```

```java
public class MenuDTO {
  private String name;
  private MenuDTO[] sub;
}
```

In this case, the following properties will be copied:

+ `Menu.name -> MenuDTO.name`

And the following properties will be copied after conversion:

+ `Menu.sub -> MenuDTO.sub`

_By the way, even if these two types are reversed, it's still works._
