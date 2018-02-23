/*******************************************************************************
 * Copyright 2018 Samsung Electronics All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.vertex;

import java.util.Map;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSource;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.ezmq.EzmqSource;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.file.FileInputSource;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq.ZmqSource;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq.common.ZmqConnectionConfig;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq.common.ZmqConnectionConfig.Builder;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.Vertex;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.schema.DataSetSchema;
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
    if (!properties.containsKey("dataType") || !properties.containsKey("dataSource")) {
      throw new NullPointerException("dataType and dataSource must be specified");
    }
    String type = ((String) properties.get("dataType"));
    String source = ((String) properties.get("dataSource"));

    if (type == null || source == null) {
      throw new NullPointerException("Null sink error");
    }

    if (type.equals("") || source.equals("")) {
      throw new NullPointerException("Empty sink error");
    }

    type = type.toLowerCase();
    String[] dataSource = source.split(":");
    if (type.equals("zmq")) {
      ZmqConnectionConfig zmqConnectionConfig = new Builder()
          .setHost(dataSource[0].trim())
          .setPort(Integer.parseInt(dataSource[1].trim()))
          .setIoThreads(1)
          .build();

      return env.addSource(new ZmqSource<>(zmqConnectionConfig,
          dataSource[2], new DataSetSchema())).setParallelism(1);
    } else if (type.equals("ezmq")) {
      String host = dataSource[0].trim();
      int port = Integer.parseInt(dataSource[1].trim());
      if (dataSource.length == 3) {
        String topic = dataSource[2].trim();
        return env.addSource(new EzmqSource(host, port, topic)).setParallelism(1);
      } else {
        return env.addSource(new EzmqSource(host, port)).setParallelism(1);
      }
    } else if (type.equals("f")) {
      String meta = ((String) properties.get("name"));
      return env.addSource(new FileInputSource(source, meta)).setParallelism(1);
    } else {
      throw new UnsupportedOperationException("Unsupported input data type: " + type);
    }
  }

  @Override
  public void setInflux(DataStream<DataSet> influx) {
    return;
  }

}
