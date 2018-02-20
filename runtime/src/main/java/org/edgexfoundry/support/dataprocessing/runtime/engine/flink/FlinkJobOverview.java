package org.edgexfoundry.support.dataprocessing.runtime.engine.flink;

import java.util.List;

public class FlinkJobOverview {

  private List<FlinkJob> running;
  private List<FlinkJob> finished;

  public List<FlinkJob> getRunning() {
    return running;
  }

  public void setRunning(List<FlinkJob> running) {
    this.running = running;
  }

  public List<FlinkJob> getFinished() {
    return finished;
  }

  public void setFinished(List<FlinkJob> finished) {
    this.finished = finished;
  }
}
