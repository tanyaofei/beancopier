package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.annotation.Property;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * This is a wrapper class for the default instance of {@link BeanCopierImpl}. The methods defined in this class can be used for bean copying.
 * If there are personalized requirements, such as copy an object which in a module that is not exported, an instance can also be created by using {@code new BeanCopierImpl()}.
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
   * Initialize a target object and copy the fields from the source object to it.
   *
   * @param source source object
   * @param target target class
   * @param <S>    the type of source
   * @param <T>    the type of target
   * @return a target object
   * @see BeanCopierImpl#copy(Object, Class)
   * @since 0.0.1
   */
  public static <S, T> T copy(S source, Class<T> target) {
    return theCopier.copy(source, target, null);
  }


  /**
   * Initialize a target object and copy the fields from the source object to it.
   * <pre>{@code
   * Source source = new Source();
   * source.setVal("val);
   * Target target = BeanCopier.copy(s, Target.class, (s, t) -> t.setVal2("val2"));
   * }</pre>
   *
   * @param source   source object
   * @param target   target class
   * @param consumer define action to be done after copied
   * @param <S>      the type of source
   * @param <T>      the type of target
   * @return the instance of target
   * @see BeanCopierImpl#copy(Object, Class, BiConsumer)
   * @since 0.0.1
   */
  public static <S, T> T copy(S source, Class<T> target, BiConsumer<S, T> consumer) {
    return theCopier.copy(source, target, consumer);
  }

  /**
   * Clone an object.
   * <p>{@link Property#value()} will not take effect.</p>
   *
   * @param source the object to be cloned
   * @param <T>    the type of object
   * @return the cloned object
   * @see BeanCopierImpl#clone(Object)
   */
  public static <T> T clone(T source) {
    return theCopier.clone(source);
  }

  /**
   * Clone objects.
   * <p>{@link Property#value()} will not take effect.</p>
   *
   * @param sources the objects to be cloned
   * @param <T>     the type of object
   * @return The cloned objects, the return values has the same size as sources.
   */
  public static <T> List<T> cloneList(Collection<T> sources) {
    return theCopier.cloneList(sources, null);
  }

  /**
   * Clone objects.
   * <p>{@link Property#value()} will not take effect.</p>
   *
   * @param sources  the objects to be cloned
   * @param consumer the action to be done after each object is cloned.
   * @param <T>      the type of object
   * @return The cloned objects, the return values has the same size as sources.
   * @see BeanCopierImpl#cloneList(Collection, BiConsumer)
   */
  public static <T> List<T> cloneList(Collection<T> sources, BiConsumer<T, T> consumer) {
    return theCopier.cloneList(sources, consumer);
  }

  /**
   * Copy objects.
   *
   * @param sources source objects
   * @param target  target class
   * @param <S>     the type of source
   * @param <T>     the type of target
   * @return The instances of target. The return values has the same size as sources.
   * @see BeanCopierImpl#copyList(Collection, Class)
   * @since 0.0.1
   */
  public static <S, T> List<T> copyList(Collection<S> sources, Class<T> target) {
    return theCopier.copyList(sources, target);
  }

  /**
   * Copy objects.
   *
   * @param source   source objects
   * @param target   target class
   * @param consumer the action to be done after each source is copied.
   * @param <S>      the type of source
   * @param <T>      the type of target
   * @return The instances of target, the return values has the same size as sources.
   * @see BeanCopierImpl#copyList(Collection, Class, BiConsumer)
   * @since 0.0.1
   **/
  public static <S, T> List<T> copyList(Collection<S> source, Class<T> target, BiConsumer<S, T> consumer) {
    return theCopier.copyList(source, target, consumer);
  }


}
