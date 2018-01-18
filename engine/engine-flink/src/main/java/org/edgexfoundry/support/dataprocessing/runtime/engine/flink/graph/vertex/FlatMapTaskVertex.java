package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.vertex;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.Vertex;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.task.FlatMapTask;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;

public class FlatMapTaskVertex implements Vertex {

  private DataStream<DataSet> influx = null;
  private TopologyProcessor taskInfo;

  public FlatMapTaskVertex(TopologyProcessor taskInfo) {
    this.taskInfo = taskInfo;
  }

  @Override
  public Long getId() {
    return this.taskInfo.getId();
  }

  @Override
  public DataStream<DataSet> serve() {
    return influx.flatMap(new FlatMapTask(taskInfo.getConfig().getProperties()));
  }

  @Override
  public void setInflux(DataStream<DataSet> influx) {
    if (this.influx == null) {
      this.influx = influx;
    } else {
      this.influx = this.influx.union(influx);
    }
  }
}
