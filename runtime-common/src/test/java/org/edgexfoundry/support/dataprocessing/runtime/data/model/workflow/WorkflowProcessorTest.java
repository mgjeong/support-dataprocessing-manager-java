package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import org.junit.Assert;
import org.junit.Test;

public class WorkflowProcessorTest {

  @Test
  public void testConstructor() {
    WorkflowProcessor processor = new WorkflowProcessor();
    Assert.assertNotNull(processor);
  }
}
