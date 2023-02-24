package io.github.tanyaofei.beancopier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * {@link BeanCopier} and {@link BeanCopierImpl} are tools for copying object({@link Object} and {@link Record}) properties.
 * For ease of use, {@link BeanCopier} is provided, which maintains a default instance of {@link BeanCopierImpl},
 * and all static methods here are calls to that instance. If you need to configure features of {@link BeanCopierImpl},
 * you can use {@link BeanCopierImpl#BeanCopierImpl(Consumer)} to create an instance with specific features.
 *
 * @author tanyaofei
 * @see BeanCopierImpl
 * @since 0.0.1
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BeanCopier {

  /**
   * Default instance of {@link BeanCopierImpl}
   */
  private final static BeanCopierImpl theCopier = BeanCopierImpl.getInstance();

  /**
   * @see BeanCopierImpl#clone(Object)
   * @see 0.1.0
   */
  public static <T> T clone(T source) {
    return theCopier.clone(source);
  }

  public static <T> T clone(T source, BiConsumer<T, T> afterCloned) {
    return theCopier.clone(source, afterCloned);
  }

  /**
   * @see BeanCopierImpl#copy(Object, Class)
   * @since 0.0.1
   */
  public static <S, T> T copy(S source, Class<T> target) {
    return theCopier.copy(source, target, (BiConsumer<S, T>) null);
  }

  /**
   * @see BeanCopierImpl#copy(Object, Class, BiConsumer)
   * @since 0.0.1
   */
  public static <S, T> T copy(S source, Class<T> target, BiConsumer<S, T> afterCopied) {
    return theCopier.copy(source, target, afterCopied);
  }

  /**
   * @see BeanCopierImpl#cloneList(Collection)
   * @since 0.1.0
   */
  public static <T> List<T> cloneList(Collection<T> sources) {
    return theCopier.cloneList(sources, null);
  }

  /**
   * @see BeanCopierImpl#cloneList(Collection, BiConsumer)
   * @see 0.1.0
   */
  public static <T> List<T> cloneList(Collection<T> sources, BiConsumer<T, T> afterEachCloned) {
    return theCopier.cloneList(sources, afterEachCloned);
  }

  /**
   * @see BeanCopierImpl#copyList(Collection, Class)
   * @since 0.0.1
   */
  public static <S, T> List<T> copyList(Collection<S> sources, Class<T> target) {
    return theCopier.copyList(sources, target);
  }

  /**
   * @see BeanCopierImpl#copyList(Collection, Class, BiConsumer)
   * @since 0.0.1
   **/
  public static <S, T> List<T> copyList(Collection<S> source, Class<T> target, BiConsumer<S, T> afterEachCopied) {
    return theCopier.copyList(source, target, afterEachCopied);
  }


}
