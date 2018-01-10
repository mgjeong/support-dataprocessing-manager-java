package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

public class TopologyJob extends Format {

  private String id;
  private String jobGroupId;
  private String engineId;
  private String data; //?
  private TopologyJobState jobState;

  public TopologyJob() {

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getJobGroupId() {
    return jobGroupId;
  }

  public void setJobGroupId(String jobGroupId) {
    this.jobGroupId = jobGroupId;
  }

  public String getEngineId() {
    return engineId;
  }

  public void setEngineId(String engineId) {
    this.engineId = engineId;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public TopologyJobState getJobState() {
    return jobState;
  }

  public void setJobState(
      TopologyJobState jobState) {
    this.jobState = jobState;
  }
}
