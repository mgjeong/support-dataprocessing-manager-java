package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.common;

import org.junit.Assert;
import org.junit.Test;

public class ZmqConnectionConfigTest {
    @Test
    public void testConstructor() {
        ZmqConnectionConfig config = new ZmqConnectionConfig("localhost", 5555, 1);
        Assert.assertNotNull(config);
        Assert.assertEquals("tcp://localhost:5555", config.getConnectionAddress());
        Assert.assertEquals(1, config.getIoThreads());
    }

    @Test
    public void testBuilder() {
        ZmqConnectionConfig.Builder builder = new ZmqConnectionConfig.Builder();
        builder.setHost("localhost");
        builder.setPort(5555);
        builder.setIoThreads(1);
        ZmqConnectionConfig config = builder.build();

        Assert.assertNotNull(config);
        Assert.assertEquals("tcp://localhost:5555", config.getConnectionAddress());
        Assert.assertEquals(1, config.getIoThreads());
    }
}
