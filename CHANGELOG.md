# 更新日志

+ 0.1.5
  + 新增 `new BeanCopierImpl(builder -> builder.preferNested)` 等更多配置
  + **修复生成转换器时生成类名可能出现死循环的问题**
  + 更多预检测

+ 0.1.4
  + 支持通过 `new BeanCopierImpl(new MyClassLoader())` 创建指定类加载器的 `BeanCopierImpl`
  + `BeanCopier` 的类加载器由原来的 `ConverterClassLoader` 修改为自动选取
  + 修复集合嵌套拷贝元素包含 `null` 时会抛出异常的问题
  + 优化拷贝效率并减少内存占用
  + `asm` 依赖库升级到 `9.4``

+ 0.1.3
  + 大幅度优化批量拷贝的速度 `BeanCopier.cloneList()` 和 `BeanCopier.copyList()`, 拷贝一百万个对象由 `200ms+`
    缩减到 `20ms+`
  + 修复 `BeanCopier.cloneList` 第一个元素为 null 时会出现 `NullPointerException` 的 bug

+ 0.1.2
  + 除了提供 `BeanCopier` 的静态方法以外，现在可 `new BeanCopierImpl()` 来创建拷贝对象，适用于有类卸载需求的场景
  + 更多测试用例

+ 0.1.1
  + 修正一些 bug
  + 完全的泛型兼容, `List<Integer>` 现在可以拷贝到 `List<? extends Number>`, `IntegerBox extends Box<Integer>`
    现在可以拷贝到 `Box<Integer>`

+ 0.1.0
  + 新增 `@Property` 注解支持字段别名和跳过字段选项

+ 0.0.8
  + lombok 依赖 `scope` 修改为 `provided`

+ 0.0.7
  + 不拷贝提供了参数数量不正确的 `getter` 和 `setter`

+ 0.0.5
  + 基础类型 `boolean` 的 `getter` 方法修改为 `isXxxx()`