package io.github.tanyaofei.beancopier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;

/**
 * 对象拷贝器
 * <p>
 * 为了方便简单使用, 此类维护着一个{@link BeanCopierImpl} 的默认实例 {@link BeanCopierImpl#getInstance()}. 可以使用这个类定义的静态方法进行对象拷贝, 也可以通过 {@code new BeanCopierImpl()} 自行创建.
 * 如果在项目中使用有类卸载需求, 则应该自行创建 {@link BeanCopierImpl}, 那么类卸载的流程应当如下:
 * </p>
 * <ol>
 *   <li>{@link BeanCopierImpl} 实例不再引用并被 GC 掉</li>
 *   <li>{@link BeanCopierImpl} 持有的 {@link ConverterFactory} 和 {@link ConverterClassLoader} 被 GC 掉</li>
 *   <li>{@link ConverterClassLoader} 装载的类被卸载 </li>
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

  private final static BeanCopierImpl IMPL = BeanCopierImpl.getInstance();

  /**
   * {@link BeanCopierImpl#copy(Object, Class)}
   */
  public static <S, T> T copy(S source, Class<T> targetClass) {
    return IMPL.copy(source, targetClass, null);
  }

  /**
   * {@link BeanCopierImpl#copy(Object, Class, Callback)}
   */
  public static <S, T> T copy(S source, Class<T> targetClass, Callback<S, T> callback) {
    return IMPL.copy(source, targetClass, callback);
  }

  /**
   * {@link BeanCopierImpl#clone(Object)}
   */
  public static <T> T clone(T source) {
    return IMPL.clone(source);
  }

  /**
   * {@link BeanCopierImpl#cloneList(Collection)}
   */
  public static <T> List<T> cloneList(Collection<T> sources) {
    return IMPL.cloneList(sources, null);
  }

  /**
   * {@link BeanCopierImpl#cloneList(Collection, Callback)}
   */
  public static <T> List<T> cloneList(Collection<T> sources, Callback<T, T> callback) {
    return IMPL.cloneList(sources, callback);
  }

  /**
   * {@link BeanCopierImpl#copyList(Collection, Class)}
   */
  public static <S, T> List<T> copyList(Collection<S> sources, Class<T> targetClass) {
    return IMPL.copyList(sources, targetClass);
  }

  /**
   * {@link BeanCopierImpl#copyList(Collection, Class, Callback)}
   */
  public static <S, T> List<T> copyList(Collection<S> source, Class<T> targetClass, Callback<S, T> callback) {
    return IMPL.copyList(source, targetClass, callback);
  }


}
