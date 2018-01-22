package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import org.junit.Assert;
import org.junit.Test;

public class WorkflowSourceTest {

  @Test
  public void testConstructor() {
    WorkflowSource source = new WorkflowSource();
    Assert.assertNotNull(source);
  }
}
