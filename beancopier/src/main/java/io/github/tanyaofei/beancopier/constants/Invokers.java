package io.github.tanyaofei.beancopier.constants;

import io.github.tanyaofei.beancopier.core.invoker.ExecutableInvoker;
import io.github.tanyaofei.beancopier.core.invoker.MethodInvoker;

/**
 * @author tanyaofei
 */
public interface Invokers {

  MethodInvoker AbstractConverter$convertAllToList = ExecutableInvoker.invoker(Methods.AbstractConverter$convertAllToList());

  MethodInvoker AbstractConverter$convertAllToArrayList = ExecutableInvoker.invoker(Methods.AbstractConverter$convertAllToArrayList());

  MethodInvoker AbstractConverter$convertAllToLinkedList = ExecutableInvoker.invoker(Methods.AbstractConverter$convertAllToLinkedList());
  MethodInvoker AbstractConverter$convertAllToSet = ExecutableInvoker.invoker(Methods.AbstractConverter$convertAllToSet());

}
