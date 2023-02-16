package io.github.tanyaofei.beancopier.utils.reflection.member;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

/**
 * @author tanyaofei
 */
public class RecordBeanWithSetterMember extends RecordBeanMember {
  public RecordBeanWithSetterMember(RecordComponent rc) {
    super(rc, null);
  }

  @Override
  public Method getMethod() {
    throw new UnsupportedOperationException("Record class has no setters");
  }

}