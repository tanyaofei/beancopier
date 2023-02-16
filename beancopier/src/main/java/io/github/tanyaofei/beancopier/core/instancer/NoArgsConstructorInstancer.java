package io.github.tanyaofei.beancopier.core.instancer;

import io.github.tanyaofei.beancopier.constants.LocalOpcode;
import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.invoker.ConstructorInvoker;
import io.github.tanyaofei.beancopier.core.invoker.ExecutableInvoker;
import io.github.tanyaofei.beancopier.core.local.IfNonNull;
import io.github.tanyaofei.beancopier.utils.reflection.member.BeanMember;
import lombok.var;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Set;

/**
 * This instancer will instantiate the target using no-args-constructor,
 * and then use setters to assign values for all fields that need to be copied.
 *
 * @author tanyaofei
 */
public class NoArgsConstructorInstancer implements TargetInstancer {

  private final MethodVisitor v;
  private final ConverterDefinition definition;
  private final int targetStore;
  private final Iterable<BeanMember> targetMembers;
  private final int firstLocalStore;
  private final Set<BeanMember> skippedMembers;

  public NoArgsConstructorInstancer(
      MethodVisitor v,
      ConverterDefinition definition,
      int targetStore,
      Iterable<BeanMember> targetMembers,
      Set<BeanMember> skippedMembers,
      int firstLocalStore
  ) {
    this.v = v;
    this.definition = definition;
    this.targetStore = targetStore;
    this.targetMembers = targetMembers;
    this.firstLocalStore = firstLocalStore;
    this.skippedMembers = skippedMembers;
  }

  @Override
  public void instantiate() {
    ConstructorInvoker.fromNoArgsConstructor(definition.getTargetType()).invoke(v);
    v.visitVarInsn(Opcodes.ASTORE, targetStore);
    int store = firstLocalStore;
    for (var member : targetMembers) {
      boolean skip = skippedMembers.contains(member);
      if (skip) {
        store += LocalOpcode.ofType(member.getType()).slots;
      } else {
        store = setValue(member, store);
      }
    }
  }

  private int setValue(BeanMember member, int localStore) {
    var os = LocalOpcode.ofType(member.getType());
    if (definition.getConfiguration().isSkipNull() && !member.getType().isPrimitive()) {
      // If skipNull is configured as true, a null check will be performed before calling the setter.
      // The setter method will only be called if the value is not null.
      new IfNonNull(
          v,
          () -> {
            v.visitVarInsn(Opcodes.ALOAD, targetStore);
            v.visitVarInsn(os.loadOpcode, localStore);
          },
          () -> ExecutableInvoker.invoker(member.getMethod()).invoke(v, true)
      ).write();
    } else {
      v.visitVarInsn(Opcodes.ALOAD, targetStore);
      v.visitVarInsn(os.loadOpcode, localStore);
      ExecutableInvoker.invoker(member.getMethod()).invoke(v, true);
    }
    return localStore + os.slots;
  }


}
