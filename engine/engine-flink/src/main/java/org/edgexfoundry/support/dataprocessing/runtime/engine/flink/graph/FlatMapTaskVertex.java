package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TaskFormat;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.task.FlatMapTask;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.operator.TaskFlatMap;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;

public class FlatMapTaskVertex implements Vertex {
  private DataStream<DataSet> influx;
  private TaskFormat taskInfo;

  public FlatMapTaskVertex(TaskFormat taskInfo) {
    this.taskInfo = taskInfo;
  }

  @Override
  public int getId() {
    return this.taskInfo.getId();
  }

  @Override
  public DataStream<DataSet> serve() {
    return influx.flatMap(new FlatMapTask(taskInfo));
  }

  @Override
  public void setFluxIn(DataStream<DataSet> influx) {
    this.influx = influx;
  }

}
