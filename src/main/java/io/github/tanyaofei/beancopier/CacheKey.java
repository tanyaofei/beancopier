package io.github.tanyaofei.beancopier;


import lombok.AllArgsConstructor;

import java.util.Objects;

/**
 * 缓存 Key 对象, 用这个比拼接 String 要快，并且更节省内存
 *
 * @author tanyaofei
 * @since 0.1.4
 */
@AllArgsConstructor
class CacheKey {

  final Class<?> sc;
  final Class<?> tc;

  @Override
  public int hashCode() {
    return Objects.hash(sc, tc);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof CacheKey)) {
      return false;
    }

    CacheKey o = (CacheKey) obj;
    return o.sc.equals(sc) && o.tc.equals(tc);
  }

}
