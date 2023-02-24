package io.github.tanyaofei.beancopier.utils.reflection.member;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

/**
 * @author tanyaofei
 */
public class SettableRecordMember extends RecordMember {

  public SettableRecordMember(@Nonnull RecordComponent rc) {
    super(rc, null);
  }

  @Nullable
  @Override
  public Method getMethod() {
    throw new UnsupportedOperationException("Record class has no setters");
  }

}