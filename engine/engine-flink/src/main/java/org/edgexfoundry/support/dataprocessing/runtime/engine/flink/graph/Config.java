package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph;

import java.util.Collections;
import java.util.List;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet.COL;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.DataFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TaskFormat;

public class Config {

  List<DataFormat> sources;
  List<DataFormat> sinks;
  List<TaskFormat> tasks;
  List<Edge> edges;

  public List<DataFormat> getSinks() {
    if (sinks == null) {
      return Collections.emptyList();
    }
    return sinks;
  }

  public void setSinks(List<DataFormat> sinks) {
    this.sinks = sinks;
  }

  public List<TaskFormat> getTasks() {
    if (tasks == null) {
      return Collections.emptyList();
    }
    return tasks;

  }

  public void setTasks(List<TaskFormat> tasks) {
    this.tasks = tasks;
  }

  public List<Edge> getEdges() {
    if (edges == null) {
      return Collections.emptyList();
    }
    return edges;
  }

  public void setEdges(List<Edge> edges) {
    this.edges = edges;
  }

  public List<DataFormat> getSources() {
    if (sources == null) {
      return Collections.emptyList();
    }
    return sources;
  }

  public void setSources(List<DataFormat> sources) {
    this.sources = sources;
  }

}
