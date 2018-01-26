package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq;

import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq.common.ZmqConnectionConfig;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq.common.ZmqConnectionConfig.Builder;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.junit.Test;

import java.nio.charset.Charset;

public class ZmqSinkTest {

    @Test
    public void testConstructor() {
        Builder builder = new Builder();
        builder.setHost("localhost").setPort(5588).setIoThreads(1);
        ZmqConnectionConfig config = builder.build();
        new ZmqSink(config, "topic", new SimpleStringSchema(Charset.defaultCharset()));
    }

    @Test
    public void testConnection() throws Exception {
        ZmqConnectionConfig.Builder builder = new ZmqConnectionConfig.Builder();
        builder.setHost("localhost").setPort(5588).setIoThreads(1);
        ZmqConnectionConfig config = builder.build();
        ZmqSink sink = new ZmqSink(config, "topic", new SimpleStringSchema(Charset.defaultCharset()));
        sink.open(null);
        sink.close();
    }

    @Test(timeout = 3000L)
    public void testInvoke() throws Exception {
        ZmqConnectionConfig.Builder builder = new ZmqConnectionConfig.Builder();
        builder.setHost("localhost").setPort(5588).setIoThreads(1);
        ZmqConnectionConfig config = builder.build();
        ZmqSink sink = new ZmqSink(config, "topic", new SimpleStringSchema(Charset.defaultCharset()));
        sink.open(null);
        sink.invoke("Hello World!");
        sink.close();
    }
}
