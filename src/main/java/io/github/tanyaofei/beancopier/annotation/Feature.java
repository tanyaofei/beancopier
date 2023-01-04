package io.github.tanyaofei.beancopier.annotation;

public enum Feature {

    /**
     * 自动装箱和拆箱, 如果包含此配置, 则在拷贝对象时, 包装类和非包装类之间可以进行拷贝, 但是如果从一个 null 拷贝为非包装类, 将会抛出 {@link NullPointerException}
     * // TODO: 当抛出 {@link NullPointerException} 时提供更详细的信息
     */
    AUTO_BOXING_AND_UNBOXING;

    public boolean isSetIn(Property property) {
        for (Feature feature : property.feature()) {
            if (feature == this) {
                return true;
            }
        }
        return false;
    }

}
