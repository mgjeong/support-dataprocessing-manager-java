package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph;

import java.util.Map;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.DataFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologySource;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.ezmq.EZMQSource;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.schema.DataSetSchema;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.ZMQSource;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.common.ZMQConnectionConfig;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.common.ZMQConnectionConfig.Builder;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;

public class SourceVertex implements Vertex {
  private StreamExecutionEnvironment env;
  private TopologySource config;

  public SourceVertex(StreamExecutionEnvironment env, TopologySource config) {
    this.env = env;
    this.config = config;
  }

  @Override
  public Long getId() {
    return this.config.getId();
  }

  @Override
  public DataStream<DataSet> serve() {
    Map<String, Object> properties = this.config.getConfig().getProperties();
    String dataType = ((String) properties.get("dataType")).toLowerCase();
    String source = ((String) properties.get("dataSource"));
    if (dataType.equals("zmq")) {
      String[] dataSource = source.split(":");
      ZMQConnectionConfig zmqConnectionConfig = new Builder()
          .setHost(dataSource[0].trim())
          .setPort(Integer.parseInt(dataSource[1].trim()))
          .setIOThreads(1)
          .build();

      return env.addSource(new ZMQSource<>(zmqConnectionConfig,
          dataSource[2], new DataSetSchema())).setParallelism(1);
    } else if (dataType.equals("ezmq")) {
      String[] dataSource = source.split(":");
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
