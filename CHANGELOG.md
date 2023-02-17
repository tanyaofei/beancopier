# 更新日志

+ 0.2.0
  + Added support for `record`
  + Added Support for JDK module system
  + Whenever possible, `Converter` will be defined as a hidden class
  + <b>BreakingChanges: </b>
    + The minimum JDK version required is 17;
    + Copy aliases will not take effect when cloning;
    + Callback action after copying is no longer done using `Callback`, but with `BiConsumer` instead;
+ 0.2.0
  + 支持 `record`
  + 支持 JDK 模块化系统
  + 在可能的情况下，`Converter` 会被定义为隐藏类
  + <b>不兼容的变化:</b>
    + 要求最低 JDK 版本 17
    + 当拷贝为克隆拷贝时，别名不再生效
    + 拷贝后回调动作不再使用 `Callback` 而是使用 `BiConsumer`
