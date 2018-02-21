/*******************************************************************************
 * Copyright 2018 Samsung Electronics All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.edgexfoundry.support.dataprocessing.runtime.data.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(Include.NON_NULL)
public class JobState extends Format {

  private State state;
  private Long startTime;
  private Long finishTime;
  private String engineId;
  private String engineType;
  private String errorMessage;
  private String host;
  private int port;

  private final String jobId;
  public JobState(String jobId) {
    this.jobId = jobId;
    this.state = State.CREATED;
  }

  public String getHost() {
    return host;
  }

  public JobState setHost(String host) {
    this.host = host;
    return this;
  }

  public int getPort() {
    return port;
  }

  public JobState setPort(int port) {
    this.port = port;
    return this;
  }

  public Long getFinishTime() {
    return finishTime;
  }

  public JobState setFinishTime(Long finishTime) {
    this.finishTime = finishTime;
    return this;
  }

  public State getState() {
    return state;
  }

  public JobState setState(State state) {
    this.state = state;
    return this;
  }

  public JobState setState(String state) {
    this.state = State.toState(state);
    if (this.state == null) {
      throw new RuntimeException("Failed to update state to " + state);
    }
    return this;
  }

  public Long getStartTime() {
    return startTime;
  }

  public JobState setStartTime(Long startTime) {
    this.startTime = startTime;
    return this;
  }

  public String getEngineId() {
    return engineId;
  }

  public JobState setEngineId(String engineId) {
    this.engineId = engineId;
    return this;
  }

  public String getEngineType() {
    return engineType;
  }

  public JobState setEngineType(String engineType) {
    this.engineType = engineType;
    return this;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public JobState setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  public String getJobId() {
    return jobId;
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
}
