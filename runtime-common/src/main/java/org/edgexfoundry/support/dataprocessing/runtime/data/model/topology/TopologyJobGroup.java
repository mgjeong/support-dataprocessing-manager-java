package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(Include.NON_NULL)
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

  @JsonIgnore
  public void addJob(TopologyJob job) {
    if (job != null) {
      this.jobs.add(job);
    }
  }

  @JsonIgnore
  public void addJobs(Collection<TopologyJob> jobs) {
    if (jobs != null && !jobs.isEmpty()) {
      this.jobs.addAll(jobs);
    }
  }

  public static TopologyJobGroup create(TopologyData topologyData) {
    if(topologyData == null){
      throw new RuntimeException("Topology data is null.");
    }

    TopologyJobGroup jobGroup = new TopologyJobGroup();
    jobGroup.setId(UUID.randomUUID().toString());
    jobGroup.setTopologyId(topologyData.getTopologyId());
    return jobGroup;
  }
}
