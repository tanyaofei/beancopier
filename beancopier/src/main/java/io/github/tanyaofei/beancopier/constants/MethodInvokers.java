package io.github.tanyaofei.beancopier.constants;

import io.github.tanyaofei.beancopier.core.invoker.ExecutableInvoker;
import io.github.tanyaofei.beancopier.core.invoker.MethodInvoker;

/**
 * @author tanyaofei
 */
public interface MethodInvokers {

  MethodInvoker Stream$map = ExecutableInvoker.invoker(Methods.Stream$map());

  MethodInvoker Stream$collect = ExecutableInvoker.invoker(Methods.Stream$collect());

  MethodInvoker Collection$stream = ExecutableInvoker.invoker(Methods.Collection$stream());

  MethodInvoker Collector$toList = ExecutableInvoker.invoker(Methods.Collectors$toList());

}
