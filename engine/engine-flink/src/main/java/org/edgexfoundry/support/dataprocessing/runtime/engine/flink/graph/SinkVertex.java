package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph;

import java.util.Map;
import org.apache.flink.shaded.com.google.common.base.Strings;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.DataFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologySink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.ezmq.EZMQSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.schema.DataSetSchema;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.sink.FileOutputSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.sink.MongoDBSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.sink.WebSocketServerSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.ZMQSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.common.ZMQConnectionConfig;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.common.ZMQConnectionConfig.Builder;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;

public class SinkVertex implements Vertex {
  TopologySink config;
  DataStream<DataSet> influx;

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
      ZMQConnectionConfig zmqConnectionConfig = new Builder()
          .setHost(dataSource[0].trim())
          .setPort(Integer.parseInt(dataSource[1].trim()))
          .setIOThreads(1)
          .build();

      influx.addSink(new ZMQSink<>(zmqConnectionConfig, dataSource[2], new DataSetSchema()))
          .setParallelism(1);
    } else if (type.equals("ws")) {
      String[] dataSource = sink.split(":");
      influx.addSink(new WebSocketServerSink(Integer.parseInt(dataSource[1])))
          .setParallelism(1);
    } else if (type.equals("ezmq")) {
      String[] dataSource = sink.split(":");
      int port = Integer.parseInt(dataSource[1].trim());
      influx.addSink(new EZMQSink(port)).setParallelism(1);
    } else if (type.equals("f")) {
      String outputFilePath = sink;
      if (!outputFilePath.endsWith(".txt")) {
        outputFilePath += ".txt";
      }
      influx.addSink(new FileOutputSink(outputFilePath));
    } else if (type.equals("mongodb")) {
      String name = ((String) properties.get("name"));
      influx.addSink(new MongoDBSink(sink, name))
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
