package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.vertex;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.Vertex;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.task.FlatMapTask;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;

public class FlatMapTaskVertex implements Vertex {

  private DataStream<DataSet> influx = null;
  private WorkflowProcessor taskInfo;

  public FlatMapTaskVertex(WorkflowProcessor taskInfo) {
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
      if (this.influx != influx) {
        this.influx = this.influx.union(influx);
      }
    }
  }
}
