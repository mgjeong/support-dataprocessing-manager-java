package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.DataFormat;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.ezmq.EZMQSource;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.schema.DataSetSchema;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.ZMQSource;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.common.ZMQConnectionConfig;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.common.ZMQConnectionConfig.Builder;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;

public class SourceVertex implements Vertex {

  private StreamExecutionEnvironment env;
  private DataFormat dataConfig;

  public SourceVertex(StreamExecutionEnvironment env, DataFormat dataConfig) {
    this.env = env;
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

      return env.addSource(new ZMQSource<>(zmqConnectionConfig,
          dataSource[2], new DataSetSchema())).setParallelism(1);
    } else if (dataType.equals("ezmq")) {
      String[] dataSource = dataConfig.getDataSource().split(":", 3);
      String host = dataSource[0].trim();
      int port = Integer.parseInt(dataSource[1].trim());
      if (dataSource.length == 3) {
        String topic = dataSource[2].trim();
        return env.addSource(new EZMQSource(host, port, topic)).setParallelism(1);
      } else {
        return env.addSource(new EZMQSource(host, port)).setParallelism(1);
      }
    } else {
      throw new RuntimeException("Unsupported input data type: " + dataType);
    }
  }

  @Override
  public void setFluxIn(DataStream<DataSet> in) {
    return;
  }

}
