package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.DataFormat;
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

  DataFormat dataConfig;
  DataStream<DataSet> influx;

  public SinkVertex(DataFormat dataConfig) {
    this.dataConfig = dataConfig;
  }

  @Override
  public int getId() {
    return this.dataConfig.getId();
  }

  @Override
  public DataStream<DataSet> serve() {
    String dataType = dataConfig.getDataType().toLowerCase();
    if (dataType.equals("zmq")) {
      String[] dataSource = dataConfig.getDataSource().split(":");
      ZMQConnectionConfig zmqConnectionConfig = new Builder()
          .setHost(dataSource[0].trim())
          .setPort(Integer.parseInt(dataSource[1].trim()))
          .setIOThreads(1)
          .build();

      influx.addSink(new ZMQSink<>(zmqConnectionConfig, dataSource[2], new DataSetSchema()))
          .setParallelism(1);
    } else if (dataType.equals("ws")) {
      String[] dataSource = dataConfig.getDataSource().split(":");
      influx.addSink(new WebSocketServerSink(Integer.parseInt(dataSource[1])))
          .setParallelism(1);
    } else if (dataType.equals("ezmq")) {
      String[] dataSource = dataConfig.getDataSource().split(":");
      int port = Integer.parseInt(dataSource[1].trim());
      influx.addSink(new EZMQSink(port)).setParallelism(1);
    } else if (dataType.equals("f")) {
      String outputFilePath = dataConfig.getDataSource();
      if (!outputFilePath.endsWith(".txt")) {
        outputFilePath += ".txt";
      }
      influx.addSink(new FileOutputSink(outputFilePath));
    } else if (dataType.equals("mongodb")) {
      influx.addSink(new MongoDBSink(dataConfig.getDataSource(), dataConfig.getName()))
          .setParallelism(1);
    } else {
      throw new RuntimeException("Unsupported output data type: " + dataType);
    }

    return null;
  }

  @Override
  public void setFluxIn(DataStream<DataSet> in) {
    this.influx = in;
  }
}
