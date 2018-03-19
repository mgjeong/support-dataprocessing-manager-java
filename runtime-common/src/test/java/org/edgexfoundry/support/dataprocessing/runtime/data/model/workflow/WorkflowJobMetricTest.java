package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import java.util.ArrayList;
import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState.State;
import org.junit.Assert;
import org.junit.Test;

public class WorkflowJobMetricTest {

  @Test
  public void testGetterAndSetter() {
    WorkflowJobMetric jobMetric = new WorkflowJobMetric();
    JobState jobState = new JobState("sampleJobId");
    jobState.setState(State.RUNNING);

    List<JobState> jobStateList = new ArrayList<>();
    jobStateList.add(jobState);

    jobMetric.setJobStates(jobStateList);
    Assert.assertEquals(1, jobMetric.getJobStates().size());

    jobMetric.setGroupId(13L);
    Assert.assertEquals(13L, jobMetric.getGroupId(), 0);
  }
}
