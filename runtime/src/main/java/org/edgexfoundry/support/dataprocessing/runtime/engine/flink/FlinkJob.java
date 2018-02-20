package org.edgexfoundry.support.dataprocessing.runtime.engine.flink;

import com.google.gson.annotations.SerializedName;

public class FlinkJob {

  private String jid;
  private String name;
  private String state;
  @SerializedName("start-time")
  private long startTime;
  @SerializedName("end-time")
  private long endTime;
  private long duration;
  @SerializedName("last_modification")
  private long lastModification;

  private FlinkTask tasks;

  public String getJid() {
    return jid;
  }

  public void setJid(String jid) {
    this.jid = jid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public long getLastModification() {
    return lastModification;
  }

  public void setLastModification(long lastModification) {
    this.lastModification = lastModification;
  }

  public FlinkTask getTasks() {
    return tasks;
  }

  public void setTasks(FlinkTask tasks) {
    this.tasks = tasks;
  }

  public long getStarttime() {
    return startTime;
  }

  public void setStarttime(long starttime) {
    this.startTime = starttime;
  }

  public long getEndtime() {
    return endTime;
  }

  public void setEndtime(long endtime) {
    this.endTime = endtime;
  }
}