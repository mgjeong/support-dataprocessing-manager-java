package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;

public interface Vertex {
  Long getId();

  DataStream<DataSet> serve() throws Exception;

  void setInflux(DataStream<DataSet> influx);
}
