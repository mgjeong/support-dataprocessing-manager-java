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
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.ezmq.EzmqSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.file.FileOutputSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.mongodb.MongoDbSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.websocket.WebSocketServerSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq.ZmqSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq.common.ZmqConnectionConfig;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq.common.ZmqConnectionConfig.Builder;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.Vertex;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.schema.DataSetSchema;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;

public class SinkVertex implements Vertex {

  private WorkflowSink config;
  private DataStream<DataSet> influx = null;

  public SinkVertex(WorkflowSink config) {
    this.config = config;
  }

  @Override
  public Long getId() {
    return this.config.getId();
  }

  @Override
  public DataStream<DataSet> serve() throws Exception {
    Map<String, Object> properties = this.config.getConfig().getProperties();
    if (!properties.containsKey("dataType") || !properties.containsKey("dataSink")) {
      throw new NullPointerException("dataType and dataSink must be specified");
    }
    String type = ((String) properties.get("dataType"));
    String sink = ((String) properties.get("dataSink"));

    if (type == null || sink == null) {
      throw new NullPointerException("Null sink error");
    }

    if (type.equals("") || sink.equals("")) {
      throw new NullPointerException("Empty sink error");
    }

    type = type.toLowerCase();
    String[] dataSink = sink.split(":");
    if (type.equals("zmq")) {
      ZmqConnectionConfig zmqConnectionConfig = new Builder()
          .setHost(dataSink[0].trim())
          .setPort(Integer.parseInt(dataSink[1].trim()))
          .setIoThreads(1)
          .build();

      influx.addSink(new ZmqSink<>(zmqConnectionConfig, dataSink[2], new DataSetSchema()))
          .setParallelism(1);
    } else if (type.equals("ws")) {
      influx.addSink(new WebSocketServerSink(Integer.parseInt(dataSink[1])))
          .setParallelism(1);
    } else if (type.equals("ezmq")) {
      int port = Integer.parseInt(dataSink[1].trim());
      influx.addSink(new EzmqSink(port)).setParallelism(1);
    } else if (type.equals("f")) {
      String outputFilePath = sink;
      if (!outputFilePath.endsWith(".txt")) {
        outputFilePath += ".txt";
      }
      influx.addSink(new FileOutputSink(outputFilePath));
    } else if (type.equals("mongodb")) {
      String[] name = ((String) properties.get("name")).split(":", 2);
      influx.addSink(new MongoDbSink(dataSink[0], Integer.parseInt(dataSink[1]), name[0], name[1]))
          .setParallelism(1);
    } else {
      throw new UnsupportedOperationException("Unsupported output data type: " + type);
    }

    return null;
  }

  @Override
  public void setInflux(DataStream<DataSet> influx) {
    if (this.influx == null) {
      this.influx = influx;
    } else {
      this.influx = this.influx.union(influx).flatMap((dataSet, collector) ->
          collector.collect(dataSet)
      );
    }
  }
}
