package org.edgexfoundry.support.dataprocessing.runtime.engine.flink;

public class FlinkTask {

  private int total;
  private int pending;
  private int running;
  private int finished;
  private int canceling;
  private int canceled;
  private int failed;

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public int getPending() {
    return pending;
  }

  public void setPending(int pending) {
    this.pending = pending;
  }

  public int getRunning() {
    return running;
  }

  public void setRunning(int running) {
    this.running = running;
  }

  public int getFinished() {
    return finished;
  }

  public void setFinished(int finished) {
    this.finished = finished;
  }

  public int getCanceling() {
    return canceling;
  }

  public void setCanceling(int canceling) {
    this.canceling = canceling;
  }

  public int getFailed() {
    return failed;
  }

  public void setFailed(int failed) {
    this.failed = failed;
  }

  public int getCanceled() {
    return canceled;
  }

  public void setCanceled(int canceled) {
    this.canceled = canceled;
  }
}
