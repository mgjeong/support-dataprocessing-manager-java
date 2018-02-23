package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowMetric.GroupInfo;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowMetric.Work;
import org.junit.Assert;
import org.junit.Test;

public class WorkflowMetricTest {

  @Test
  public void testGetterAndSetter() {
    WorkflowMetric workflowMetric = new WorkflowMetric();
    workflowMetric.setGroups(Collections.emptyList());
    Assert.assertEquals(0, workflowMetric.getGroups().size());

    // work
    WorkflowMetric.Work work = new Work();
    work.setRunning(12);
    work.setStop(3);
    Assert.assertEquals(12, work.getRunning());
    Assert.assertEquals(3, work.getStop());

    work = new Work(12, 3);
    Assert.assertEquals(12, work.getRunning());
    Assert.assertEquals(3, work.getStop());

    // group info
    List<GroupInfo> groupInfoList = new ArrayList<>();
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setGroupId("1");
    groupInfo.setWorks(work);
    Assert.assertEquals("1", groupInfo.getGroupId());
    Assert.assertEquals(work, groupInfo.getWorks());

    groupInfoList.add(groupInfo);
    workflowMetric.setGroups(groupInfoList);
    Assert.assertEquals(1, workflowMetric.getGroups().size());
  }
}
