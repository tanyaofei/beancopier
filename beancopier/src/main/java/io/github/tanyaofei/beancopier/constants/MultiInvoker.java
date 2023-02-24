package io.github.tanyaofei.beancopier.constants;

import io.github.tanyaofei.beancopier.core.invoker.ExecutableInvoker;
import lombok.AllArgsConstructor;
import org.objectweb.asm.MethodVisitor;

/**
 * @author tanyaofei
 */
@AllArgsConstructor
public class MultiInvoker implements ExecutableInvoker {

  private ExecutableInvoker first;

  private ExecutableInvoker next;

  @Override
  public void invoke(MethodVisitor v, boolean popReturnValue) {
    first.invoke(v);
    next.invoke(v, popReturnValue);
  }

}
