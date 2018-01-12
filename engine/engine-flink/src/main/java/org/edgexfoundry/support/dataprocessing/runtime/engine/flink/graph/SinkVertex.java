package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph;

import java.util.Map;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologySink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.ezmq.EzmqSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.schema.DataSetSchema;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.sink.FileOutputSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.sink.MongoDbSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.sink.WebSocketServerSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.ZmqSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.common.ZmqConnectionConfig;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.common.ZmqConnectionConfig.Builder;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;

public class SinkVertex implements Vertex {

  private TopologySink config;
  private DataStream<DataSet> influx = null;

  public SinkVertex(TopologySink config) {
    this.config = config;
  }

  @Override
  public Long getId() {
    return this.config.getId();
  }

  @Override
  public DataStream<DataSet> serve() throws Exception {
    Map<String, Object> properties = this.config.getConfig().getProperties();
    String type = ((String) properties.get("dataType")).toLowerCase();
    String sink = ((String) properties.get("dataSink"));

    if (type.equals("") || sink.equals("")) {
      throw new IllegalStateException("Empty sink error");
    }

    if (type == null || sink == null) {
      throw new IllegalStateException("Null sink error");
    }

    if (type.equals("zmq")) {
      String[] dataSource = sink.split(":");
      ZmqConnectionConfig zmqConnectionConfig = new Builder()
          .setHost(dataSource[0].trim())
          .setPort(Integer.parseInt(dataSource[1].trim()))
          .setIoThreads(1)
          .build();

      influx.addSink(new ZmqSink<>(zmqConnectionConfig, dataSource[2], new DataSetSchema()))
          .setParallelism(1);
    } else if (type.equals("ws")) {
      String[] dataSource = sink.split(":");
      influx.addSink(new WebSocketServerSink(Integer.parseInt(dataSource[1])))
          .setParallelism(1);
    } else if (type.equals("ezmq")) {
      String[] dataSource = sink.split(":");
      int port = Integer.parseInt(dataSource[1].trim());
      influx.addSink(new EzmqSink(port)).setParallelism(1);
    } else if (type.equals("f")) {
      String outputFilePath = sink;
      if (!outputFilePath.endsWith(".txt")) {
        outputFilePath += ".txt";
      }
      influx.addSink(new FileOutputSink(outputFilePath));
    } else if (type.equals("mongodb")) {
      String name = ((String) properties.get("name"));
      influx.addSink(new MongoDbSink(sink, name))
          .setParallelism(1);
    } else {
      throw new RuntimeException("Unsupported output data type: " + type);
    }

    return null;
  }

  @Override
  public void setFluxIn(DataStream<DataSet> in) {
    this.influx = in;
  }
}
