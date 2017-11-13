package com.sec.processing.framework.engine.flink.zmq;

import com.sec.processing.framework.engine.flink.zmq.common.ZMQConnectionConfig;
import com.sec.processing.framework.engine.flink.zmq.common.ZMQUtil;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.util.serialization.SerializationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

public class ZMQSink<IN> extends RichSinkFunction<IN> {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ZMQSink.class);

    private final ZMQConnectionConfig zmqConnectionConfig;
    private final String topic;
    private SerializationSchema<IN> schema;

    private ZMQ.Context zmqContext;
    private ZMQ.Socket zmqSocket;

    public ZMQSink(ZMQConnectionConfig zmqConnectionConfig, String topic, SerializationSchema<IN> schema) {
        this.zmqConnectionConfig = zmqConnectionConfig;
        this.topic = topic;
        this.schema = schema;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        this.zmqContext = ZMQ.context(this.zmqConnectionConfig.getIoThreads());
        this.zmqSocket = this.zmqContext.socket(ZMQ.PUB);

        // Attempt to bind first, connect if bind fails.
        LOGGER.info("Binding ZMQ to {}", this.zmqConnectionConfig.getConnectionAddress());
        this.zmqSocket.bind(this.zmqConnectionConfig.getConnectionAddress());
    }

    @Override
    public void close() throws Exception {
        super.close();

        if (this.zmqSocket != null) {
            this.zmqSocket.close();
        }

        if (this.zmqContext != null) {
            this.zmqContext.close();
        }
    }

    @Override
    public void invoke(IN in) throws Exception {
        byte[] msg = schema.serialize(in);
        this.zmqSocket.sendMore(this.topic);
        this.zmqSocket.send(ZMQUtil.encode(msg));
    }
}
