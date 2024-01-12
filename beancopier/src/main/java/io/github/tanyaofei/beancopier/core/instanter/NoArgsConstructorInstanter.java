package io.github.tanyaofei.beancopier.core.instanter;

import io.github.tanyaofei.beancopier.constants.TypedOpcode;
import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.invoker.ConstructorInvoker;
import io.github.tanyaofei.beancopier.core.invoker.ExecutableInvoker;
import io.github.tanyaofei.beancopier.core.local.IfNonNull;
import io.github.tanyaofei.beancopier.utils.reflection.member.BeanMember;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Set;

/**
 * This instancer will instantiate the target using no-args-constructor,
 * and then use setters to assign values for all fields that need to be copied.
 *
 * @author tanyaofei
 */
public class NoArgsConstructorInstanter implements TargetInstanter {

  @NotNull
  private final MethodVisitor mv;

  @NotNull
  private final ConverterDefinition definition;

  private final int targetStore;

  @NotNull
  private final Iterable<? extends BeanMember> consumers;

  private final int firstLocalStore;

  @NotNull
  private final Set<? extends BeanMember> skippedConsumers;

  public NoArgsConstructorInstanter(
      @NotNull MethodVisitor mv,
      @NotNull ConverterDefinition definition,
      int targetStore,
      @NotNull Iterable<? extends BeanMember> consumers,
      @NotNull Set<? extends BeanMember> skippedConsumers,
      int firstLocalStore
  ) {
    this.mv = mv;
    this.definition = definition;
    this.targetStore = targetStore;
    this.consumers = consumers;
    this.firstLocalStore = firstLocalStore;
    this.skippedConsumers = skippedConsumers;
  }

  @Override
  public void instantiate() {
    ConstructorInvoker.fromNoArgsConstructor(definition.getTargetType()).invoke(mv);
    mv.visitVarInsn(Opcodes.ASTORE, targetStore);
    int store = firstLocalStore;
    for (var consumer : consumers) {
      boolean skip = skippedConsumers.contains(consumer);
      if (skip) {
        store += TypedOpcode.ofType(consumer.getType().getRawType()).slots;
      } else {
        store = consume(consumer, store);
      }
    }
  }

  private int consume(@NotNull BeanMember member, int localStore) {
    var os = TypedOpcode.ofType(member.getType().getRawType());
    if (definition.getFeatures().isSkipNull() && !member.getType().getRawType().isPrimitive()) {
      // If skipNull is configured as true, a null check will be performed before calling the setter.
      // The setter method will only be called if the value is not null.
      new IfNonNull(
          mv,
          () -> {
            mv.visitVarInsn(Opcodes.ALOAD, targetStore);
            mv.visitVarInsn(os.load, localStore);
          },
          () -> ExecutableInvoker.invoker(member.getMethod()).invoke(mv, true)
      ).write();
    } else {
      mv.visitVarInsn(Opcodes.ALOAD, targetStore);
      mv.visitVarInsn(os.load, localStore);
      ExecutableInvoker.invoker(member.getMethod()).invoke(mv, true);
    }
    return localStore + os.slots;
  }


}
