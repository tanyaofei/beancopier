package io.github.tanyaofei.beancopier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;

/**
 * 对象拷贝器
 * <p>
 * 为了方便简单使用, 此类维护着一个{@link BeanCopierImpl} 的默认实例 {@link Lazy#IMPL}. 可以使用这个类定义的静态方法进行对象拷贝, 也可以通过 {@code new BeanCopierImpl()} 自行创建.
 * 如果在项目中使用有类卸载需求, 则应该自行创建 {@link BeanCopierImpl}, 那么类卸载的流程应当如下:
 * </p>
 * <ol>
 *   <li>1. {@link BeanCopierImpl} 实例不再引用并被 GC 掉</li>
 *   <li>2. {@link BeanCopierImpl} 持有的 {@link ConverterFactory} 和 {@link ConverterClassLoader} 被 GC 掉</li>
 *   <li>3. {@link ConverterClassLoader} 装载的类被卸载 </li>
 * </ol>
 * <p>即当 {@link BeanCopierImpl} 实例不再被引用时, 该类在运行时生成的转换器类都会被卸载</p>
 * <p><i>注意: 类的卸载依赖垃圾回收器具有类卸载能力</i></p>
 *
 * @author tanyaofei
 * @see BeanCopierImpl
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BeanCopier {

  /**
   * {@linkplain BeanCopierImpl#copy(Object, Class)}
   */
  public static <S, T> T copy(S source, Class<T> targetClass) {
    return Lazy.IMPL.copy(source, targetClass, null);
  }

  /**
   * {@linkplain BeanCopierImpl#clone(Object)}
   */
  public static <T> T clone(T source) {
    return Lazy.IMPL.clone(source);
  }

  /**
   * {@linkplain BeanCopierImpl#cloneList(Collection)}
   */
  public static <T> List<T> cloneList(Collection<T> sources) {
    return Lazy.IMPL.cloneList(sources, null);
  }

  /**
   * {@linkplain BeanCopierImpl#cloneList(Collection, Callback)}
   */
  public static <T> List<T> cloneList(Collection<T> sources, Callback<T, T> callback) {
    return Lazy.IMPL.cloneList(sources, callback);
  }

  /**
   * {@linkplain BeanCopierImpl#copyList(Collection, Class)}
   */
  public static <S, T> List<T> copyList(Collection<S> sources, Class<T> targetClass) {
    return Lazy.IMPL.copyList(sources, targetClass);
  }

  /**
   * {@linkplain BeanCopierImpl#copyList(Collection, Class, Callback)}
   */
  public static <S, T> List<T> copyList(Collection<S> source, Class<T> targetClass, Callback<S, T> callback) {
    return Lazy.IMPL.copyList(source, targetClass, callback);
  }

  /**
   * {@linkplain BeanCopierImpl#copy(Object, Class, Callback)}
   */
  public static <S, T> T copy(S source, Class<T> targetClass, Callback<S, T> callback) {
    return Lazy.IMPL.copy(source, targetClass, callback);
  }

  private final static class Lazy {
    private final static BeanCopierImpl IMPL = new BeanCopierImpl();
  }

}
