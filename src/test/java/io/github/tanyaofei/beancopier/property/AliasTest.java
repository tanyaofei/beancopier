package io.github.tanyaofei.beancopier.property;

import io.github.tanyaofei.beancopier.BeanCopier;
import io.github.tanyaofei.beancopier.BeanCopierConfiguration;
import io.github.tanyaofei.beancopier.annotation.Property;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AliasTest {

    static {
        System.setProperty(BeanCopierConfiguration.PropertyNames.CONVERTER_CLASS_DUMP_PATH, "./");
    }

    @Test
    public void testAlias() {
        A a = new A()
                .setA("1")
                .setB(Arrays.asList("1", "2", "3"));
        B b = BeanCopier.copy(a, B.class);

        assertNull(b.getA1());
        assertEquals(a.getB(), b.getB1());
    }

    @Data
    @Accessors(chain = true)
    public static class A {
        private String a;
        private List<String> b;
    }

    @Data
    @Accessors(chain = true)
    public static class B {

        @Property("a")
        private Integer a1;
        @Property("b")
        private List<String> b1;
    }

}
