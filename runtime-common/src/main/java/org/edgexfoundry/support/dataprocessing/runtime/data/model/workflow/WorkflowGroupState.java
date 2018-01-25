package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState;

import java.util.ArrayList;

public class WorkflowGroupState extends Format {

  private String groupId;
  private ArrayList<JobState> jobStates;

  public ArrayList<JobState> getJobStates() {
    return jobStates;
  }

  public WorkflowGroupState setGroupId(String groupId) {
    this.groupId = groupId;
    return this;
  }

  public String getGroupId() {
    return groupId;
  }

  public WorkflowGroupState setJobStates(ArrayList<JobState> jobStates) {
    this.jobStates = jobStates;
    return this;
  }
}
