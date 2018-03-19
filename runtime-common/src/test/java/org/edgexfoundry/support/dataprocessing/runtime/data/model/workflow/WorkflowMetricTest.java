package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowMetric.Count;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowMetric.WorkflowInfo;
import org.junit.Assert;
import org.junit.Test;

public class WorkflowMetricTest {

  @Test
  public void testGetterAndSetter() {
    WorkflowMetric workflowMetric = new WorkflowMetric();
    workflowMetric.setWorkflowInfos(Collections.emptyList());
    Assert.assertEquals(0, workflowMetric.getWorkflowInfos().size());

    // count
    WorkflowMetric.Count count = new Count();
    count.setRunning(12);
    count.setStop(3);
    Assert.assertEquals(12, count.getRunning());
    Assert.assertEquals(3, count.getStop());

    count = new Count(12, 3);
    Assert.assertEquals(12, count.getRunning());
    Assert.assertEquals(3, count.getStop());

    // group info
    List<WorkflowInfo> workflowInfos = new ArrayList<>();
    WorkflowInfo workflowInfo = new WorkflowInfo();
    workflowInfo.setWorkflowId(1L);
    workflowInfo.setCount(count);
    Assert.assertEquals(1, workflowInfo.getWorkflowId(), 0);
    Assert.assertEquals(count, workflowInfo.getCount());

    workflowInfos.add(workflowInfo);
    workflowMetric.setWorkflowInfos(workflowInfos);
    Assert.assertEquals(1, workflowMetric.getWorkflowInfos().size());
  }
}
