package io.github.tanyaofei.beancopier.core.local;

import io.github.tanyaofei.beancopier.utils.reflection.member.BeanMember;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 局部变量定义上下文
 */
@Data
@Accessors(chain = true)
public class LocalsDefinitionContext {

  /**
   * 拷贝来源属性
   */
  private Map<String, BeanMember> sourceMembers;

  /**
   * 下一个局部变量存放到局部变量表中的位置
   * <p>因为 dstore, lstore 需要占用两个局部变量槽, 因此需要定义器去定义下一个的位置</p>
   */
  int nextStore;

}
