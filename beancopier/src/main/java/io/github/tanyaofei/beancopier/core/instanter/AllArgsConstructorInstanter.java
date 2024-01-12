package io.github.tanyaofei.beancopier.core.instanter;

import io.github.tanyaofei.beancopier.constants.TypedOpcode;
import io.github.tanyaofei.beancopier.core.ConverterDefinition;
import io.github.tanyaofei.beancopier.core.invoker.ExecutableInvoker;
import io.github.tanyaofei.beancopier.utils.reflection.member.BeanMember;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * This instanter will instantiate the target using the all-args-constructor,
 * and the fields to be copied will be assigned values during the construction process.
 *
 * @author tanyaofei
 */
public class AllArgsConstructorInstanter implements TargetInstanter {

  @NotNull
  private final MethodVisitor mv;

  @NotNull
  private final ConverterDefinition definition;

  private final int targetStore;

  @NotNull
  private final Iterable<? extends BeanMember> consumers;

  private final int firstLocalStore;

  public AllArgsConstructorInstanter(
      @NotNull MethodVisitor mv,
      @NotNull ConverterDefinition definition,
      int targetStore,
      @NotNull Iterable<? extends BeanMember> consumers,
      int firstLocalStore
  ) {
    this.mv = mv;
    this.definition = definition;
    this.targetStore = targetStore;
    this.consumers = consumers;
    this.firstLocalStore = firstLocalStore;
  }


  @Override
  public void instantiate() {
    ExecutableInvoker.invoker(definition.getTargetType().getConstructors()[0]).invoke(
        mv,
        () -> {
          int store = firstLocalStore;
          for (var consumer : consumers) {
            var op = TypedOpcode.ofType(consumer.getType().getRawType());
            mv.visitVarInsn(op.load, store);
            store += op.slots;
          }
        }
    );
    mv.visitVarInsn(Opcodes.ASTORE, targetStore);
  }
}
