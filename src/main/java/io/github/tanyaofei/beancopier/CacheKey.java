package io.github.tanyaofei.beancopier;


import lombok.AllArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
public class CacheKey {

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
