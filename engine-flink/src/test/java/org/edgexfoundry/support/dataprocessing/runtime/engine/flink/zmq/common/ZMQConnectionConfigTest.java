package org.edgexfoundry.processing.runtime.engine.flink.zmq.common;

import org.junit.Assert;
import org.junit.Test;

public class ZMQConnectionConfigTest {
    @Test
    public void testConstructor() {
        ZMQConnectionConfig config = new ZMQConnectionConfig("localhost", 5555, 1);
        Assert.assertNotNull(config);
        Assert.assertEquals("tcp://localhost:5555", config.getConnectionAddress());
        Assert.assertEquals(1, config.getIoThreads());
    }

    @Test
    public void testBuilder() {
        ZMQConnectionConfig.Builder builder = new ZMQConnectionConfig.Builder();
        builder.setHost("localhost");
        builder.setPort(5555);
        builder.setIOThreads(1);
        ZMQConnectionConfig config = builder.build();

        Assert.assertNotNull(config);
        Assert.assertEquals("tcp://localhost:5555", config.getConnectionAddress());
        Assert.assertEquals(1, config.getIoThreads());
    }
}
