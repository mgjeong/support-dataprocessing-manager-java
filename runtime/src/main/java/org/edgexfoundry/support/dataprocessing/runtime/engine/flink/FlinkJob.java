package org.edgexfoundry.support.dataprocessing.runtime.engine.flink;

import com.google.gson.annotations.SerializedName;

public class FlinkJob {

  public static String FAIL = "FAIL";

  private String jid;
  private String name;
  private String state;
  @SerializedName("start-time")
  private long startTime;
  @SerializedName("end-time")
  private long endTime;
  private long duration;
  private long last_modification;

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

  public long getLast_modification() {
    return last_modification;
  }

  public void setLast_modification(long last_modification) {
    this.last_modification = last_modification;
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