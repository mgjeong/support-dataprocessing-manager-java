package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.apache.flink.streaming.util.serialization.DeserializationSchema;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.common.ZmqConnectionConfig;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.common.ZmqUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

public class ZmqSource<OUT> extends RichSourceFunction<OUT> implements ResultTypeQueryable<OUT> {

  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = LoggerFactory.getLogger(ZmqSource.class);

  private final ZmqConnectionConfig zmqConnectionConfig;
  private final String topic;

  private DeserializationSchema<OUT> schema;

  private ZMQ.Context zmqContext = null;
  private ZMQ.Socket zmqSocket = null;

  private transient volatile boolean running;

  public ZmqSource(ZmqConnectionConfig zmqConnectionConfig, String topic,
      DeserializationSchema<OUT> deserializationSchema) {
    this.zmqConnectionConfig = zmqConnectionConfig;
    this.topic = topic;
    this.schema = deserializationSchema;
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);

    this.zmqContext = ZMQ.context(this.zmqConnectionConfig.getIoThreads());
    this.zmqSocket = this.zmqContext.socket(ZMQ.SUB);

    LOGGER.info("Connecting ZMQ to {}", this.zmqConnectionConfig.getConnectionAddress());
    this.zmqSocket.connect(this.zmqConnectionConfig.getConnectionAddress());

    LOGGER.info("Subscribing ZMQ to {}", this.topic);
    this.zmqSocket.subscribe(this.topic.getBytes());
    this.running = true;
  }

  @Override
  public void run(SourceContext<OUT> sourceContext) throws Exception {
    String data;
    while (this.running) {
      try {
        this.zmqSocket.recvStr(); // discard this, not used (message envelop)
        data = this.zmqSocket.recvStr();
        byte[] b = ZmqUtil.decode(data);
        sourceContext.collect(this.schema.deserialize(b));
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
      }
    }
  }

  @Override
  public void close() throws Exception {
    super.close();
  }

  @Override
  public void cancel() {
    if (this.zmqSocket != null) {
      this.zmqSocket.close();
    }

    if (this.zmqContext != null) {
      this.zmqContext.close();
    }

    this.running = false;
  }

  @Override
  public TypeInformation<OUT> getProducedType() {
    return this.schema.getProducedType();
  }
}
