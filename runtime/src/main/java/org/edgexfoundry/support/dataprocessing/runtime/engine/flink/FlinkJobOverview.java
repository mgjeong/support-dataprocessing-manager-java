package org.edgexfoundry.support.dataprocessing.runtime.engine.flink;

import java.util.ArrayList;

public class FlinkJobOverview {

  private ArrayList<FlinkJob> running;
  private ArrayList<FlinkJob> finished;

  public ArrayList<FlinkJob> getRunning() {
    return running;
  }

  public void setRunning(ArrayList<FlinkJob> running) {
    this.running = running;
  }

  public ArrayList<FlinkJob> getFinished() {
    return finished;
  }

  public void setFinished(ArrayList<FlinkJob> finished) {
    this.finished = finished;
  }
}
