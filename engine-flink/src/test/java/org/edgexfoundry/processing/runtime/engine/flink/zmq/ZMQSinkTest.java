package com.sec.processing.framework.engine.flink.zmq;

import com.sec.processing.framework.engine.flink.zmq.common.ZMQConnectionConfig;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.junit.Test;

import java.nio.charset.Charset;

public class ZMQSinkTest {

    @Test
    public void testConstructor() {
        ZMQConnectionConfig.Builder builder = new ZMQConnectionConfig.Builder();
        builder.setHost("localhost").setPort(5588).setIOThreads(1);
        ZMQConnectionConfig config = builder.build();
        new ZMQSink(config, "topic", new SimpleStringSchema(Charset.defaultCharset()));
    }

    @Test
    public void testConnection() throws Exception {
        ZMQConnectionConfig.Builder builder = new ZMQConnectionConfig.Builder();
        builder.setHost("localhost").setPort(5588).setIOThreads(1);
        ZMQConnectionConfig config = builder.build();
        ZMQSink sink = new ZMQSink(config, "topic", new SimpleStringSchema(Charset.defaultCharset()));
        sink.open(null);
        sink.close();
    }

    @Test(timeout = 3000L)
    public void testInvoke() throws Exception {
        ZMQConnectionConfig.Builder builder = new ZMQConnectionConfig.Builder();
        builder.setHost("localhost").setPort(5588).setIOThreads(1);
        ZMQConnectionConfig config = builder.build();
        ZMQSink sink = new ZMQSink(config, "topic", new SimpleStringSchema(Charset.defaultCharset()));
        sink.open(null);
        sink.invoke("Hello World!");
        sink.close();
    }
}
