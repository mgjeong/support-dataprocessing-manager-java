package org.edgexfoundry.support.dataprocessing.runtime.data.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(Include.NON_NULL)
public class JobState extends Format {

  public Long getFinshTime() {
    return finshTime;
  }

  public void setFinshTime(Long finshTime) {
    this.finshTime = finshTime;
  }

  public enum State {
    CREATED, RUNNING, STOPPED, ERROR;

    static State toState(String v) {
      if (v == null) {
        return null;
      }

      for (State s : State.values()) {
        if (s.name().equalsIgnoreCase(v)) {
          return s;
        }
      }
      return null;
    }
  }

  private State state;
  private Long startTime;
  private Long finshTime;
  private String engineId;
  private String engineType;
  private String errorMessage;

  public JobState() {

  }

  public State getState() {
    return state;
  }

  public void setState(String state) {
    this.state = State.toState(state);
    if (this.state == null) {
      throw new RuntimeException("Failed to update state to " + state);
    }
  }

  public void setState(State state) {
    this.state = state;
  }

  public Long getStartTime() {
    return startTime;
  }

  public void setStartTime(Long startTime) {
    this.startTime = startTime;
  }

  public String getEngineId() {
    return engineId;
  }

  public void setEngineId(String engineId) {
    this.engineId = engineId;
  }

  public String getEngineType() {
    return engineType;
  }

  public void setEngineType(String engineType) {
    this.engineType = engineType;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
