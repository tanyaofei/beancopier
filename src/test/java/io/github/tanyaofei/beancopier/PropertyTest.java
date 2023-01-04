package io.github.tanyaofei.beancopier;

import io.github.tanyaofei.beancopier.annotation.Feature;
import io.github.tanyaofei.beancopier.annotation.Property;
import io.github.tanyaofei.beancopier.typehandler.impl.arraytolist.IntegerArrayToListTypeHandler;
import io.github.tanyaofei.beancopier.typehandler.impl.arraytolist.LongArrayToListTypeHandler;
import io.github.tanyaofei.beancopier.typehandler.impl.arraytolist.ShortArrayToListTypeHandler;
import io.github.tanyaofei.beancopier.typehandler.impl.arraytolist.StringArrayToListTypeHandler;
import io.github.tanyaofei.beancopier.typehandler.impl.listtoarray.IntegerListToArrayTypeHandler;
import io.github.tanyaofei.beancopier.typehandler.impl.listtoarray.LongListToArrayTypeHandler;
import io.github.tanyaofei.beancopier.typehandler.impl.listtoarray.ShortListToArrayTypeHandler;
import io.github.tanyaofei.beancopier.typehandler.impl.listtoarray.StringListToArrayTypeHandler;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PropertyTest {


    static {
        System.setProperty(BeanCopierConfiguration.PropertyNames.CONVERTER_CLASS_DUMP_PATH, "./");
    }

    @Test
    public void testAliasAndTypeHandler() {
        Source source = new Source()
                .setA("a")
                .setB(new Integer[]{1})
                .setC(new Long[]{1L})
                .setD(new Short[]{1})
                .setE(new String[]{"1"})
                .setF(Collections.singletonList(1))
                .setG(Collections.singletonList(1L))
                .setH(Collections.singletonList((short) 1))
                .setI(Collections.singletonList("1"))
                .setJ(Collections.singletonList("1"))
                .setY("1")
                .setZ(new String[]{"1"});
        Target target = BeanCopier.copy(source, Target.class);
        assertEquals(source.getA(), target.getA1());
        assertEquals(target.getB(), Collections.singletonList(1));
        assertEquals(target.getC(), Collections.singletonList(1L));
        assertEquals(target.getD(), Collections.singletonList((short) 1));
        assertEquals(target.getE(), Collections.singletonList("1"));

        assertArrayEquals(target.getF(), new Integer[]{1});
        assertArrayEquals(target.getG(), new Long[]{1L});
        assertArrayEquals(target.getH(), new Short[]{(short) 1});
        assertArrayEquals(target.getI(), new String[]{"1"});
        assertArrayEquals(target.getJ1(), new String[]{"1"});

        assertNull(target.getY());
        assertNull(target.getZ());
    }

    @Test
    public void testAutoBoxingAndUnboxing() {
        BoxedObject boxedObject = new BoxedObject()
                .setA((short)1)
                .setB(1)
                .setC(1L)
                .setD('1')
                .setE(1F)
                .setF(1D)
                .setG(true)
                .setZ(null);

        UnboxedObject unboxedObject = BeanCopier.copy(boxedObject, UnboxedObject.class);
        assertEquals(unboxedObject.getA(), (short) 1);
        assertEquals(unboxedObject.getB(), 1);
        assertEquals(unboxedObject.getC(), 1L);
        assertEquals(unboxedObject.getD(), '1');
        assertEquals(unboxedObject.getE(), 1F);
        assertEquals(unboxedObject.getF(), 1D);
        assertTrue(unboxedObject.isG());
        assertEquals(unboxedObject.getY(), 0);
        assertEquals(unboxedObject.getZ(), 0);

        unboxedObject = new UnboxedObject()
                .setA((short) 1)
                .setB(1)
                .setC(1L)
                .setD('1')
                .setE(1F)
                .setF(1D)
                .setG(true);
        boxedObject = BeanCopier.copy(unboxedObject, BoxedObject.class);
        assertEquals(boxedObject.getA(), (short) 1);
        assertEquals(boxedObject.getB(), 1);
        assertEquals(boxedObject.getC(), 1L);
        assertEquals(boxedObject.getD(), '1');
        assertEquals(boxedObject.getE(), 1F);
        assertEquals(boxedObject.getF(), 1D);
        assertEquals(boxedObject.getG(), true);
    }

    @Data
    @Accessors(chain = true)
    public static class BoxedObject {

        @Property(feature = Feature.AUTO_BOXING_AND_UNBOXING)
        private Short a;
        @Property(feature = Feature.AUTO_BOXING_AND_UNBOXING)
        private Integer b;
        @Property(feature = Feature.AUTO_BOXING_AND_UNBOXING)
        private Long c;
        @Property(feature = Feature.AUTO_BOXING_AND_UNBOXING)
        private Character d;
        @Property(feature = Feature.AUTO_BOXING_AND_UNBOXING)
        private Float e;
        @Property(feature = Feature.AUTO_BOXING_AND_UNBOXING)
        private Double f;
        @Property(feature = Feature.AUTO_BOXING_AND_UNBOXING)
        private Boolean g;

        @Property(feature = Feature.AUTO_BOXING_AND_UNBOXING)
        private Integer z;

    }

    @Data
    @Accessors(chain = true)
    public static class UnboxedObject {
        @Property(feature = Feature.AUTO_BOXING_AND_UNBOXING)
        private short a;
        @Property(feature = Feature.AUTO_BOXING_AND_UNBOXING)
        private int b;
        @Property(feature = Feature.AUTO_BOXING_AND_UNBOXING)
        private long c;
        @Property(feature = Feature.AUTO_BOXING_AND_UNBOXING)
        private char d;
        @Property(feature = Feature.AUTO_BOXING_AND_UNBOXING)
        private float e;
        @Property(feature = Feature.AUTO_BOXING_AND_UNBOXING)
        private double f;
        @Property(feature = Feature.AUTO_BOXING_AND_UNBOXING)
        private boolean g;

        private int y;

        @Property(feature = {Feature.AUTO_BOXING_AND_UNBOXING})
        private int z;
    }

    @Data
    @Accessors(chain = true)
    public static class Source {
        private String a;
        private Integer[] b;
        private Long[] c;
        private Short[] d;
        private String[] e;

        private List<Integer> f;
        private List<Long> g;
        private List<Short> h;
        private List<String> i;
        private List<String> j;


        private String y;
        private String[] z;

    }

    @Data
    @Accessors(chain = true)
    public static class Target {

        @Property("a")
        private String a1;

        @Property(typeHandler = IntegerArrayToListTypeHandler.class)
        private List<Integer> b;
        @Property(typeHandler = LongArrayToListTypeHandler.class)
        private List<Long> c;
        @Property(typeHandler = ShortArrayToListTypeHandler.class)
        private List<Short> d;
        @Property(typeHandler = StringArrayToListTypeHandler.class)
        private List<String> e;

        @Property(typeHandler = IntegerListToArrayTypeHandler.class)
        private Integer[] f;
        @Property(typeHandler = LongListToArrayTypeHandler.class)
        private Long[] g;
        @Property(typeHandler = ShortListToArrayTypeHandler.class)
        private Short[] h;
        @Property(typeHandler = StringListToArrayTypeHandler.class)
        private String[] i;
        @Property(value = "j", typeHandler = StringListToArrayTypeHandler.class)
        private String[] j1;

        @Property("y1")
        private String y;

        @Property(typeHandler = LongListToArrayTypeHandler.class)
        private List<String> z;
    }

}
