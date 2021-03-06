package com.github.tanyaofei.beancopier;

/**
 * 对象拷贝器
 * <p>该类的实现类使用 ASM 技术动态生成</p>
 *
 * @author 谭耀飞
 * @since 2021.04.0
 */
public interface Converter<S, T> {

  /**
   * 拷贝对象
   * <p>该方法的具体实现由 asm 字节码生成, 内容为所有 target.setXX(source.getXX)</p>
   *
   * @param source 拷贝来源
   * @return T
   */
  T convert(S source);

}
