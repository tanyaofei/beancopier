//package io.github.tanyaofei.beancopier.utils.reflection.member;
//
//import io.github.tanyaofei.beancopier.utils.GenericType;
//import lombok.EqualsAndHashCode;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import java.lang.annotation.Annotation;
//import java.lang.reflect.Method;
//import java.lang.reflect.RecordComponent;
//
///**
// * @author tanyaofei
// */
//@EqualsAndHashCode
//public class RecordMember implements BeanMember {
//
//  @NotNull
//  private final RecordComponent rc;
//
//  @Nullable
//  private final Method method;
//
//  @NotNull
//  private final GenericType<?> genericType;
//
//  public RecordMember(@NotNull RecordComponent rc, @Nullable Method method) {
//    this.rc = rc;
//    this.method = method;
//    this.genericType = GenericType.of(rc);
//  }
//
//  @NotNull
//  @Override
//  public Object getIdentify() {
//    return rc;
//  }
//
//  @NotNull
//  @Override
//  public String getName() {
//    return rc.getName();
//  }
//
//  @NotNull
//  @Override
//  public GenericType<?> getType() {
//    return this.genericType;
//  }
//
//  @Override
//  @Nullable
//  public Method getMethod() {
//    return method;
//  }
//
//  @Nullable
//  @Override
//  public <T extends Annotation> T getAnnotation(@NotNull Class<T> annotationClass) {
//    return rc.getAnnotation(annotationClass);
//  }
//}