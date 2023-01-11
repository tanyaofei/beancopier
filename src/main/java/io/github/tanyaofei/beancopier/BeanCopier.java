package io.github.tanyaofei.beancopier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;

/**
 * 对象拷贝器
 * <p>
 * 为了方便简单使用，此类维护着一个{@link BeanCopierImpl} 的默认实例 {@link BeanCopierImpl#getInstance()}。 可以使用这个类定义的静态方法进行对象拷贝, 这些静态方法都是对 {@link BeanCopierImpl} 的调用。
 * 也可以通过 {@code new BeanCopierImpl()} 自行创建对象拷贝器，如果在项目中使用有类卸载需求, 则应该自行创建 {@link BeanCopierImpl},
 * 那么类卸载的流程应当如下:
 * </p>
 * <ol>
 *   <li>{@link BeanCopierImpl} 实例不再引用并被 GC 掉</li>
 *   <li>{@link BeanCopierImpl} 持有的 {@link ConverterFactory} 和 {@link DefaultClassLoader} 被 GC 掉</li>
 *   <li>{@link DefaultClassLoader} 装载的类被卸载 </li>
 * </ol>
 * <p>即当 {@link BeanCopierImpl} 实例不再被引用时, 该类在运行时生成的转换器类都会被卸载</p>
 * <p>
 *   这个 BeanCopier 生成的转换器将会由 app classloader 进行加载, 因此基本上不可能进行类卸载
 * </p>
 * <p><i>注意: 类的卸载依赖垃圾回收器具有类卸载能力</i></p>
 *
 * @author tanyaofei
 * @see BeanCopierImpl
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BeanCopier {

  private final static BeanCopierImpl theCopier = BeanCopierImpl.getInstance();


  /**
   * 拷贝对象, 实例化一个 target 并将 source 的字段拷贝到 target
   *
   * @param source      拷贝来源
   * @param target 拷贝目标类
   * @param <S>         拷贝来源类型
   * @param <T>         拷贝目标类型
   * @return 拷贝目标
   * @see BeanCopierImpl#copy(Object, Class)
   */
  public static <S, T> T copy(S source, Class<T> target) {
    return theCopier.copy(source, target, null);
  }


  /**
   * 拷贝对象, 在拷贝完之后将会调用 callback 进行回调操作
   * <pre>{@code
   * Source source = new Source();
   * source.setVal("val);
   * Target target = BeanCopier.copy(s, Target.class, (s, t) -> t.setVal2("val2"));
   * }</pre>
   *
   * @param source   拷贝来源
   * @param target   拷贝目标类
   * @param callback 回调动作
   * @param <S>      拷贝来源类
   * @param <T>      拷贝目标类
   * @return 拷贝目标
   * @see BeanCopierImpl#copy(Object, Class, Callback)
   */
  public static <S, T> T copy(S source, Class<T> target, Callback<S, T> callback) {
    return theCopier.copy(source, target, callback);
  }

  /**
   * 克隆对象
   *
   * @param source 克隆对象
   * @param <T>    克隆对象类
   * @return 克隆出来的对象
   * @see BeanCopierImpl#clone(Object)
   */
  public static <T> T clone(T source) {
    return theCopier.clone(source);
  }

  /**
   * 批量克隆对象
   *
   * @param sources 克隆对象集合，不能为 null。如果元素为 null 则拷贝出来的元素也为 null
   * @param <T>     克隆对象类
   * @return 被克隆出来的对象集合
   */
  public static <T> List<T> cloneList(Collection<T> sources) {
    return theCopier.cloneList(sources, null);
  }

  /**
   * 批量克隆对象
   *
   * @param sources  克隆对象集合, 不能为 null。如果元素为 null 则拷贝出来的元素也为 null
   * @param callback 每克隆一次对象都会调用一次 callback，要注意如果元素为 null 则 callback 中的参数也为 null
   * @return 被克隆出来的对象集合
   * @see BeanCopierImpl#cloneList(Collection, Callback)
   */
  public static <T> List<T> cloneList(Collection<T> sources, Callback<T, T> callback) {
    return theCopier.cloneList(sources, callback);
  }

  /**
   * 批量拷贝对象
   *
   * @param sources 拷贝来源集合，不可以为 null。如果元素为 null 则拷贝出来的元素也为 null
   * @param target  拷贝目标类
   * @param <S>     拷贝来源类型
   * @param <T>     拷贝目标类型
   * @return 拷贝目标集合
   * @see BeanCopierImpl#copyList(Collection, Class)
   */
  public static <S, T> List<T> copyList(Collection<S> sources, Class<T> target) {
    return theCopier.copyList(sources, target);
  }

  /**
   * 批量拷贝对象，每一个对象拷贝完之后都会调用 callback
   *
   * @param source      拷贝来源集合，不可以为 null。如果元素为 null 则拷贝出来的元素也为 null
   * @param target 拷贝目标类
   * @param callback    回调
   * @param <S>         拷贝来源类型
   * @param <T>         拷贝目标类型
   * @return 拷贝目标集合
   * @see BeanCopierImpl#copyList(Collection, Class, Callback)
   **/
  public static <S, T> List<T> copyList(Collection<S> source, Class<T> target, Callback<S, T> callback) {
    return theCopier.copyList(source, target, callback);
  }


}
