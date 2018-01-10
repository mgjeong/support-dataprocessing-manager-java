package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import java.util.ArrayList;
import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

public class TopologyJobGroup extends Format {

  private String id;
  private Long topologyId;

  private List<TopologyJob> jobs = new ArrayList<>();

  public TopologyJobGroup() {

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Long getTopologyId() {
    return topologyId;
  }

  public void setTopologyId(Long topologyId) {
    this.topologyId = topologyId;
  }

  public List<TopologyJob> getJobs() {
    return jobs;
  }

  public void setJobs(
      List<TopologyJob> jobs) {
    this.jobs = jobs;
  }
}
