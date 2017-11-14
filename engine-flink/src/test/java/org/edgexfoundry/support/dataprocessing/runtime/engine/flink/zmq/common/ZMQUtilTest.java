package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.common;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ZMQUtilTest {

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor<ZMQUtil> c = ZMQUtil.class.getDeclaredConstructor();
        c.setAccessible(true);

        try {
            c.newInstance();
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getTargetException() instanceof UnsupportedOperationException);
            return;
        }
        Assert.fail("Should not reach here.");
    }

    @Test
    public void testDecode() {
        String testString = "Hello World!";
        byte[] decoded = ZMQUtil.decode(testString);

        Assert.assertNotNull(decoded);
        Assert.assertTrue(decoded.length > 0);
    }

    @Test
    public void testDecodeInvalid() throws Exception {
        String testString = "Hello World!";

        Method m = ZMQUtil.class.getDeclaredMethod("decode", String.class, String.class);
        m.setAccessible(true);
        try {
            m.invoke(null, testString, "InvalidCharset");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getTargetException() instanceof RuntimeException);
            return;
        }
        Assert.fail("Should not reach here.");
    }

    @Test
    public void testEncode() {
        String testString = "Hello World!";
        byte[] decoded = ZMQUtil.decode(testString);

        Assert.assertNotNull(decoded);
        Assert.assertTrue(decoded.length > 0);

        String encoded = ZMQUtil.encode(decoded);

        Assert.assertNotNull(encoded);
        Assert.assertEquals(testString, encoded);
    }

    @Test
    public void testEncodingInvalid() throws Exception {
        String testString = "Hello World!";
        byte[] decoded = ZMQUtil.decode(testString);

        Assert.assertNotNull(decoded);
        Assert.assertTrue(decoded.length > 0);

        Method m = ZMQUtil.class.getDeclaredMethod("encode", byte[].class, String.class);
        m.setAccessible(true);
        try {
            m.invoke(null, decoded, "InvalidCharset");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getTargetException() instanceof RuntimeException);
            return;
        }
        Assert.fail("Should not reach here.");
    }
}
