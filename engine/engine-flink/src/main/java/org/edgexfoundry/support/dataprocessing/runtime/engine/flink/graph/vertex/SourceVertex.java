package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.vertex;

import java.util.Map;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSource;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.ezmq.EzmqSource;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.Vertex;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.schema.DataSetSchema;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq.ZmqSource;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq.common.ZmqConnectionConfig;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq.common.ZmqConnectionConfig.Builder;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;

public class SourceVertex implements Vertex {
  private StreamExecutionEnvironment env;
  private WorkflowSource config;

  public SourceVertex(StreamExecutionEnvironment env, WorkflowSource config) {
    this.env = env;
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
    String source = ((String) properties.get("dataSource"));

    if (type.equals("") || source.equals("")) {
      throw new IllegalStateException("Empty sink error");
    }

    if (type == null || source == null) {
      throw new IllegalStateException("Null sink error");
    }

    if (type.equals("zmq")) {
      String[] dataSource = source.split(":");
      ZmqConnectionConfig zmqConnectionConfig = new Builder()
          .setHost(dataSource[0].trim())
          .setPort(Integer.parseInt(dataSource[1].trim()))
          .setIoThreads(1)
          .build();

      return env.addSource(new ZmqSource<>(zmqConnectionConfig,
          dataSource[2], new DataSetSchema())).setParallelism(1);
    } else if (type.equals("ezmq")) {
      String[] dataSource = source.split(":");
      String host = dataSource[0].trim();
      int port = Integer.parseInt(dataSource[1].trim());
      if (dataSource.length == 3) {
        String topic = dataSource[2].trim();
        return env.addSource(new EzmqSource(host, port, topic)).setParallelism(1);
      } else {
        return env.addSource(new EzmqSource(host, port)).setParallelism(1);
      }
    } else {
      throw new RuntimeException("Unsupported input data type: " + type);
    }
  }

  @Override
  public void setInflux(DataStream<DataSet> influx) {
    return;
  }

}
