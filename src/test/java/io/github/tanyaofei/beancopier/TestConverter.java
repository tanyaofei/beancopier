package io.github.tanyaofei.beancopier;

public class TestConverter implements Converter<PropertyTest.BoxedObject, PropertyTest.UnboxedObject> {

    @Override
    public PropertyTest.UnboxedObject convert(PropertyTest.BoxedObject source) {
        PropertyTest.UnboxedObject var2 = new PropertyTest.UnboxedObject();
        Short a = source.getA();
        if (a == null) {
            throw new NullPointerException("source.getA() is null");
        } else {
            var2.setA(a);
        }
        return var2;
    }

    public PropertyTest.UnboxedObject convert2(PropertyTest.BoxedObject source) {
        PropertyTest.UnboxedObject var2 = new PropertyTest.UnboxedObject();
        var2.setA(source.getA());
        return var2;
    }
}
