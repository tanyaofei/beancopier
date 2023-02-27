package io.github.tanyaofei.beancopier.core.instanter;

import io.github.tanyaofei.beancopier.constants.TypedOpcode;
import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.invoker.ConstructorInvoker;
import io.github.tanyaofei.beancopier.core.invoker.ExecutableInvoker;
import io.github.tanyaofei.beancopier.core.local.IfNonNull;
import io.github.tanyaofei.beancopier.utils.reflection.member.BeanMember;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * This instancer will instantiate the target using no-args-constructor,
 * and then use setters to assign values for all fields that need to be copied.
 *
 * @author tanyaofei
 */
public class NoArgsConstructorInstanter implements TargetInstanter {

  @Nonnull
  private final MethodVisitor mv;

  @Nonnull
  private final ConverterDefinition definition;

  private final int targetStore;

  @Nonnull
  private final Iterable<? extends BeanMember> consumers;

  private final int firstLocalStore;

  @Nonnull
  private final Set<? extends BeanMember> skippedConsumers;

  public NoArgsConstructorInstanter(
      @Nonnull MethodVisitor mv,
      @Nonnull ConverterDefinition definition,
      int targetStore,
      @Nonnull Iterable<? extends BeanMember> consumers,
      @Nonnull Set<? extends BeanMember> skippedConsumers,
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

  private int consume(@Nonnull BeanMember member, int localStore) {
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
