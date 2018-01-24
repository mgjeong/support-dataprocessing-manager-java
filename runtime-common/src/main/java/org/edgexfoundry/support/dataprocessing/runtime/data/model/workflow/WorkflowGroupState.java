package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState;

import java.util.ArrayList;

public class WorkflowGroupState extends Format{

  private ArrayList<JobState> jobStates;

  public ArrayList<JobState> getJobStates() {
    return jobStates;
  }

  public void setJobStates(ArrayList<JobState> jobStates) {
    this.jobStates = jobStates;
  }
}
