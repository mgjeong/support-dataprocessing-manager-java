package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import org.junit.Assert;
import org.junit.Test;

public class WorkflowSinkTest {

  @Test
  public void testConstructor() {
    WorkflowSink sink = new WorkflowSink();
    Assert.assertNotNull(sink);
  }
}
