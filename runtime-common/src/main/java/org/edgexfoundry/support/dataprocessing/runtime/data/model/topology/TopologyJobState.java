package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;


import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

public class TopologyJobState extends Format {

  public enum State {
    CREATED, RUNNING, STOPPED;

    public static State toState(String s) {
      for (State state : State.values()) {
        if (state.name().equalsIgnoreCase(s)) {
          return state;
        }
      }
      return null;
    }
  }

  private String jobGroupId;
  private String jobId;
  private State state;
  private Long startTime;

  public TopologyJobState() {

  }

  public String getJobGroupId() {
    return jobGroupId;
  }

  public void setJobGroupId(String jobGroupId) {
    this.jobGroupId = jobGroupId;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public State getState() {
    return state;
  }

  public void setState(
      State state) {
    this.state = state;
  }

  public Long getStartTime() {
    return startTime;
  }

  public void setStartTime(Long startTime) {
    this.startTime = startTime;
  }
}
