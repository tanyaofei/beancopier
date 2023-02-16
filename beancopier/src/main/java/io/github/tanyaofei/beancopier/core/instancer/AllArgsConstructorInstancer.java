package io.github.tanyaofei.beancopier.core.instancer;

import io.github.tanyaofei.beancopier.constants.LocalOpcode;
import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.invoker.ExecutableInvoker;
import io.github.tanyaofei.beancopier.utils.reflection.member.BeanMember;
import lombok.var;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This instancer will instantiate the target using the all-args-constructor,
 * and the fields to be copied will be assigned values during the construction process.
 *
 * @author tanyaofei
 */
public class AllArgsConstructorInstancer implements TargetInstancer {

  private final MethodVisitor v;
  private final ConverterDefinition definition;
  private final int targetStore;
  private final Iterable<BeanMember> targetMembers;
  private final int firstLocalStore;

  public AllArgsConstructorInstancer(MethodVisitor v, ConverterDefinition definition, int targetStore, Iterable<BeanMember> targetMembers, int firstLocalStore) {
    this.v = v;
    this.definition = definition;
    this.targetStore = targetStore;
    this.targetMembers = targetMembers;
    this.firstLocalStore = firstLocalStore;
  }


  @Override
  public void instantiate() {
    ExecutableInvoker.invoker(definition.getTargetType().getConstructors()[0]).invoke(
        v,
        () -> {
          int store = firstLocalStore;
          for (var member : targetMembers) {
            var op = LocalOpcode.ofType(member.getType());
            v.visitVarInsn(op.loadOpcode, store);
            store += op.slots;
          }
        }
    );
    v.visitVarInsn(Opcodes.ASTORE, targetStore);
  }
}
