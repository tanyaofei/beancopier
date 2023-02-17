package io.github.tanyaofei.beancopier.constants;

import io.github.tanyaofei.beancopier.core.invoker.ExecutableInvoker;
import io.github.tanyaofei.beancopier.core.invoker.MethodInvoker;

/**
 * @author tanyaofei
 */
public interface MethodInvokers {

  MethodInvoker AbstractConverter$convertAll = ExecutableInvoker.invoker(Methods.AbstractConverter$convertAll());

}
