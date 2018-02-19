package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import java.util.ArrayList;
import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState;

public class WorkflowJobMetric extends Format {

  private Long groupId;
  private List<JobState> jobStates = new ArrayList<>();

  public List<JobState> getJobStates() {
    return jobStates;
  }

  public WorkflowJobMetric setJobStates(List<JobState> jobStates) {
    this.jobStates.clear();
    this.jobStates.addAll(jobStates);
    return this;
  }

  public Long getGroupId() {
    return groupId;
  }

  public void setGroupId(Long groupId) {
    this.groupId = groupId;
  }
}
