package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowDetailed.RunningStatus;
import org.junit.Assert;
import org.junit.Test;

public class WorkflowDetailedTest {

  @Test
  public void testSetterAndGetter() {
    WorkflowDetailed detailed = new WorkflowDetailed();
    Workflow workflow = new Workflow();
    detailed.setWorkflow(workflow);
    detailed.setRunning(RunningStatus.UNKNOWN);

    Assert.assertEquals(workflow, detailed.getWorkflow());
    Assert.assertEquals(RunningStatus.UNKNOWN, detailed.getRunning());
  }

  @Test
  public void testInvalidSetter() {
    WorkflowDetailed detailed = new WorkflowDetailed();
    try {
      detailed.setWorkflow(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // Success
    }

    try {
      detailed.setRunning(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // Success
    }
  }
}
