package io.github.tanyaofei.beancopier.constants;

import io.github.tanyaofei.beancopier.core.invoker.ExecutableInvoker;
import io.github.tanyaofei.beancopier.core.invoker.MethodInvoker;

/**
 * @author tanyaofei
 */
public interface Invokers {

  MethodInvoker AbstractConverter$convertArrayToStream = ExecutableInvoker.invoker(Methods.AbstractConverter$convertArrayToStream());
  MethodInvoker AbstractConverter$convertIterableToStream = ExecutableInvoker.invoker(Methods.AbstractConverter$convertIterableToStream());

  MethodInvoker AbstractConverter$collectToSet = ExecutableInvoker.invoker(Methods.AbstractConverter$collectToSet());
  MethodInvoker AbstractConverter$collectToHashSet = ExecutableInvoker.invoker(Methods.AbstractConverter$collectToHashSet());
  MethodInvoker AbstractConverter$collectToTreeSet = ExecutableInvoker.invoker(Methods.AbstractConverter$collectToTreeSet());


  MethodInvoker AbstractConverter$collectToList = ExecutableInvoker.invoker(Methods.AbstractConverter$collectToList());
  MethodInvoker AbstractConverter$collectToArrayList = ExecutableInvoker.invoker(Methods.AbstractConverter$collectToArrayList());
  MethodInvoker AbstractConverter$collectToLinkedList = ExecutableInvoker.invoker(Methods.AbstractConverter$collectToLinkedList());

  MethodInvoker AbstractConverter$collectToArray = ExecutableInvoker.invoker(Methods.AbstractConverter$collectToArray());

}
