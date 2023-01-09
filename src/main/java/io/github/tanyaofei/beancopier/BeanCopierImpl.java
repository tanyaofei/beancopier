package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.exception.CopyException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * BeanCopier 的实现
 * <p>
 * 通过 public 方法创建的实例都将使用自定义类加载器, 因此具有类卸载能力, 但是无法对自定义类加载器加载的对象进行拷贝, 如果有这个需要, 应当使用 {@link BeanCopier} 提供的静态方法进行拷贝.
 * {@link BeanCopier} 生成的转换器使用 app 类加载器进行加载
 * </p>
 *
 * @see io.github.tanyaofei.beancopier.exception.BeanCopierException
 * @see io.github.tanyaofei.beancopier.exception.ConverterGenerateException
 * @see io.github.tanyaofei.beancopier.exception.ConverterNewInstanceException
 * @see CopyException
 * @see ConverterFactory
 */
public class BeanCopierImpl {

  /**
   * 转换器缓存默认容量
   */
  private static final int DEFAULT_CACHES_CAPACITY = 64;

  /**
   * 转换器缓存
   */
  private final ConcurrentMap<CacheKey, ? super Converter<?, ?>> caches;

  /**
   * 转换器工厂
   */
  private final ConverterFactory converterFactory;


  /**
   * @see #BeanCopierImpl(int)
   */
  public BeanCopierImpl() {
    this(DEFAULT_CACHES_CAPACITY);
  }

  /**
   * 创建一个指定初始容量的实例, 该实例将使用 {@link DefaultClassLoader} 类加载器加载创建的类, 因此该实例无法访问当前以外的类加载器加载的类.
   * <p>
   * 如果需要拷贝或拷贝到其他类加载器加载的类应当使用 {@link #BeanCopierImpl(ClassLoader)}, 同时该类加载器必须重载父类方法 {@link ClassLoader#defineClass(String, byte[], int, int)} 来自定义你的类加载逻辑
   * </p>
   * <p>
   * <br/>
   * <pre>{@code
   *  Class c = yourClassLoader.defineClass();
   *  new BeanCopierImpl().copy(new Object(), c);  // NoClassDefFoundError
   * }</pre>
   * <p>
   * <br/><pre>{@code
   *  Class c = yourClassLoader.defineClass();
   *  new BeanCopierImpl(yourClassLoader).copy(new Object(), c);   // ok
   * }
   *
   * </pre>
   *
   * @param cachesInitialCapacity 初始缓存容量 {@link ConcurrentHashMap#ConcurrentHashMap(int)}
   */
  @Contract(pure = true)
  public BeanCopierImpl(int cachesInitialCapacity) {
    this(
        cachesInitialCapacity,
        new DefaultClassLoader(BeanCopierImpl.class.getClassLoader())
    );
  }

  /**
   * @param classLoader 类加载器
   * @see #BeanCopierImpl(int, ClassLoader)
   */
  @Contract(pure = true)
  public BeanCopierImpl(@NotNull ClassLoader classLoader) {
    this(
        DEFAULT_CACHES_CAPACITY,
        classLoader
    );
  }

  /**
   * 创建一个指定缓存初始容量和类加载器的实例, 之后生成的转换器类都将会使用该类加载器进行加载
   *
   * @param cachesInitialCapacity 缓存初始容量
   * @param classLoader           类加载器
   */
  @Contract(pure = true)
  public BeanCopierImpl(int cachesInitialCapacity, @NotNull ClassLoader classLoader) {
    this(
        cachesInitialCapacity,
        new ConverterFactory(classLoader, NamingPolicy.getDefault(), BeanCopierConfiguration.CONVERTER_CLASS_DUMP_PATH)
    );
  }

  /**
   * @param cachesInitialCapacity 初始缓存容量 {@link ConcurrentHashMap#ConcurrentHashMap(int)}
   * @param converterFactory      转换器工厂
   */
  @Contract(pure = true)
  private BeanCopierImpl(int cachesInitialCapacity, @NotNull ConverterFactory converterFactory) {
    this.caches = new ConcurrentHashMap<>(cachesInitialCapacity);
    this.converterFactory = converterFactory;
  }

  /**
   * 获取一个单例, 该单例使用 app classloader 来加载转换器类, 所以加载的类是不会释放
   *
   * @return BeanCopier 单例
   */
  @Contract(pure = true)
  static BeanCopierImpl getInstance() {
    return Lazy.INSTANCE;
  }

  /**
   * 拷贝对象
   *
   * @param source      拷贝来源, 如果该参数为 null 则返回 null
   * @param targetClass 拷贝目标类
   * @param <S>         拷贝来源类
   * @param <T>         拷贝目标类
   * @return 拷贝结果
   * @see #copy(Object, Class, Callback)
   */
  @Contract(value = "null, _ -> null", pure = true)
  public <S, T> T copy(@Nullable S source, @NotNull Class<T> targetClass) {
    return copy(source, targetClass, null);
  }

  /**
   * 对象拷贝, 会将类型兼容并名称相同的字段逐一拷贝到结果中去, 以下字段可以成功拷贝
   * <ul>
   *   <li>相同类型, 如 String -> String, Source -> Source</li>
   *   <li>类型兼容, 如 Integer -> Number, List&lt;Integer&gt; -> List&lt;Number&gt;, ArrayList&lt;Integer&gt -> List&lt;Number&gt;</li>
   *   <li>递归拷贝, 如 Source -> Target </li>
   *   <li>列表递归拷贝, 如 List&lt;Source&gt; -> List&lt;Target&gt;</li>
   *   <li>父类字段拷贝</li>
   * </ul>
   *
   * <p>使用 {@link io.github.tanyaofei.beancopier.annotation.Property} 将会影响上述拷贝的结果</p>
   * <p>如果需要批量拷贝对象, 使用 {@link #copyList(Collection, Class)} 可以提供更好的性能</p>
   *
   * @param source      拷贝来源
   * @param targetClass 拷贝目标类
   * @param <S>         拷贝来源
   * @param <T>         拷贝目标
   * @param callback    拷贝完之后进行的操作
   * @return 拷贝结果
   * @throws CopyException 如果拷贝过程中发生异常
   * @see io.github.tanyaofei.beancopier.annotation.Property 属性配置
   * @see ConverterFactory#generateConverter(Class, Class) 动态生成 Source to Target 的转换器
   */
  @SuppressWarnings("unchecked")
  @Contract(value = "null, _, _ -> null", pure = true)
  public <S, T> T copy(
      @Nullable S source,
      @NotNull Class<T> targetClass,
      @Nullable Callback<S, T> callback
  ) {
    if (source == null) {
      return null;
    }

    Class<S> sc = (Class<S>) source.getClass();
    Converter<S, T> converter = generateConverter(new CacheKey(sc, targetClass), sc, targetClass);

    // init a t, and copy fields from source
    T t;
    try {
      t = converter.convert(source);
      if (callback != null) {
        callback.apply(source, t);
      }
    } catch (Exception e) {
      throw new CopyException("Failed to copy bean with the following converter: " + converter.getClass().getName(), e);
    }

    return t;
  }

  /**
   * 克隆对象
   * <p>如果需要批量去克隆对象的, 使用 {@link #cloneList(Collection)} 可以提供更好的性能</p>
   *
   * @param source 被克隆对象, 如果该参数为 null 则返回 null
   * @param <T>    克隆对象类
   * @return 克隆结果
   */
  @SuppressWarnings("unchecked")
  @Contract(value = "null -> null", pure = true)
  public <T> T clone(@Nullable T source) {
    if (source == null) {
      return null;
    }
    return copy(source, (Class<T>) source.getClass(), null);
  }


  /**
   * 批量克隆对象
   *
   * @param sources 被克隆对象集合, 该集合不能为 null, 但集合的元素可以为 null
   * @param <T>     克隆元素类
   * @return 克隆结果集合
   * @see #cloneList(Collection, Callback)
   */
  @NotNull
  @Contract(pure = true)
  public <T> List<T> cloneList(@NotNull Collection<@Nullable T> sources) {
    return cloneList(sources, null);
  }

  /**
   * 批量克隆对象
   *
   * @param objs     被克隆对象集合, 该集合不能为 null, 但集合的元素可以为 null
   * @param callback 如果这个参数不为 null 时, 每一个克隆对象被克隆时都会调用此接口
   * @param <T>      克隆对象类
   * @return {@link ArrayList} 克隆结果列表
   * @see #copy(Object, Class, Callback)
   * @see #copyList(Collection, Class, Callback)
   */
  @Contract(pure = true)
  @SuppressWarnings("unchecked")
  public <T> List<T> cloneList(
      @NotNull Collection<@Nullable T> objs,
      @Nullable Callback<T, T> callback
  ) {
    if (objs.isEmpty()) {
      return new ArrayList<>();
    }

    int remains = objs.size();
    List<T> ret = new ArrayList<>(remains);

    if (objs instanceof List) {
      ListIterator<T> itr = ((List<T>) objs).listIterator();
      while (itr.hasNext()) {
        T t = itr.next();
        if (t == null) {
          remains--;
          ret.add(null);
          continue;
        }

        itr.previous();
        Class<T> c = (Class<T>) t.getClass();
        assert remains + ret.size() == objs.size();
        ret.addAll(copyList(itr, c, callback, remains));
      }
    } else {
      Iterator<T> itr = objs.iterator();
      while (itr.hasNext()) {
        T t = itr.next();
        if (t == null) {
          remains--;
          ret.add(null);
          continue;
        }

        Class<T> c = (Class<T>) t.getClass();
        ret.add(copy(t, c, callback));
        assert remains - 1 + ret.size() == objs.size();
        ret.addAll(copyList(itr, c, callback, remains - 1));
        return ret;
      }
    }

    assert objs.size() == ret.size();
    return ret;
  }

  /**
   * 批量对象拷贝
   *
   * @param sources     拷贝来源集合
   * @param targetClass 拷贝目标类
   * @param <S>         拷贝来源类
   * @param <T>         拷贝目标类
   * @return {@link ArrayList} 拷贝结果列表
   */
  @Contract(pure = true)
  public <S, T> List<T> copyList(
      @NotNull Collection<@Nullable S> sources,
      @NotNull Class<T> targetClass
  ) {
    return copyList(sources, targetClass, null);
  }

  /**
   * 批量对象拷贝
   *
   * @param source      拷贝来源
   * @param targetClass 拷贝目标类
   * @param callback    列表里每个对象拷贝完之后进行的回调操作
   * @param <S>         拷贝来源类
   * @param <T>         拷贝目标类
   * @return {@link ArrayList} 拷贝结果列表
   * @throws CopyException 如果拷贝过程中发生异常
   */
  @NotNull
  @Contract(pure = true)
  public <S, T> List<T> copyList(
      @NotNull Collection<@Nullable S> source,
      @NotNull Class<T> targetClass,
      @Nullable Callback<S, T> callback
  ) {
    return copyList(source.iterator(), targetClass, callback, source.size());
  }

  private static class Lazy {

    /**
     * 默认实现由于没有类卸载需求, 因此使用 app classloader 来加载转换器类
     */
    private final static BeanCopierImpl INSTANCE = new BeanCopierImpl(
        DEFAULT_CACHES_CAPACITY,
        new ConverterFactory(null,
            NamingPolicy.getDefault(),
            BeanCopierConfiguration.CONVERTER_CLASS_DUMP_PATH)
    );
  }

  /**
   * 批量对象拷贝
   *
   * @param itr             拷贝来源, 这个方法会调用 {@link Iterator#next()}, 因此该参数的状态有可能发生变化
   * @param tc              拷贝目标类
   * @param callback        列表里每个对象拷贝完之后进行的回调操作
   * @param initialCapacity 返回结果列表初始化容量
   * @param <S>             拷贝来源类
   * @param <T>             拷贝目标类
   * @return {@link ArrayList} 拷贝结果列表
   */
  @NotNull
  @SuppressWarnings("unchecked")
  private <S, T> List<T> copyList(
      @NotNull Iterator<@Nullable S> itr,
      @NotNull Class<T> tc,
      @Nullable Callback<S, T> callback,
      int initialCapacity
  ) {
    if (!itr.hasNext()) {
      return new ArrayList<>();
    }

    // 因为 itr 可能一直返回 null, 因此要一直迭代到第一个不为 null 才能正确获取 class
    Converter<S, T> c = null;
    List<T> ret = initialCapacity > 0 ? new ArrayList<>(initialCapacity) : new ArrayList<>();
    while (itr.hasNext()) {
      S s = itr.next();
      T t;
      if (s == null) {
        t = null;
      } else if (c == null) {
        Class<S> sc = (Class<S>) s.getClass();
        CacheKey cacheKey = new CacheKey(sc, tc);
        c = generateConverter(cacheKey, sc, tc);
        t = c.convert(s);
      } else {
        t = c.convert(s);
      }

      ret.add(t);
      if (callback != null) {
        callback.apply(s, t);
      }
    }

    return ret;
  }




  /**
   * @param cacheKey 缓存 Key
   * @param s   拷贝来源类
   * @param t   拷贝目标类
   * @param <S>      拷贝来源类
   * @param <T>      拷贝目标类
   * @return 来源拷贝到目标的转换器
   */
  @SuppressWarnings("unchecked")
  private <S, T> Converter<S, T> generateConverter(
      @NotNull CacheKey cacheKey,
      @NotNull Class<S> s,
      @NotNull Class<T> t
  ) {
    return (Converter<S, T>) caches.computeIfAbsent(
        cacheKey,
        key -> converterFactory.generateConverter(s, t)
    );
  }

}
