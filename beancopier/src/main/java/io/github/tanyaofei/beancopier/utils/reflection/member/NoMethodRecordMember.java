package io.github.tanyaofei.beancopier.utils.reflection.member;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

/**
 * @author tanyaofei
 */
public class NoMethodRecordMember extends RecordMember {

  public NoMethodRecordMember(@Nonnull RecordComponent rc) {
    super(rc, null);
  }

  @Nullable
  @Override
  public Method getMethod() {
    throw new UnsupportedOperationException("getMethod");
  }

}