package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.UUID;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(Include.NON_NULL)
public class TopologyJob extends Format {

  private String id;
  private String groupId;
  private String engineId;
  private String data;

  private TopologyJobState state;

  public TopologyJob() {

  }

  public TopologyJobState getState() {
    return state;
  }

  public void setState(
      TopologyJobState state) {
    this.state = state;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
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

  public static TopologyJob create(String jobGroupId) {
    TopologyJob job = new TopologyJob();
    job.setId(UUID.randomUUID().toString());
    job.setState(new TopologyJobState());
    job.setGroupId(jobGroupId);
    return job;
  }
}
