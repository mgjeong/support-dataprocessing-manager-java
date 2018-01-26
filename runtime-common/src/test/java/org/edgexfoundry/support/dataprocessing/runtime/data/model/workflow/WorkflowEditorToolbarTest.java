package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import org.junit.Assert;
import org.junit.Test;

public class WorkflowEditorToolbarTest {

  @Test
  public void testSetterAndGetter() {
    long now = System.currentTimeMillis();

    WorkflowEditorToolbar toolbar = new WorkflowEditorToolbar();
    toolbar.setUserId(1L);
    toolbar.setData("{}");
    toolbar.setTimestamp(now);

    Assert.assertEquals(1L, toolbar.getUserId().longValue());
    Assert.assertEquals("{}", toolbar.getData());
    Assert.assertEquals(now, toolbar.getTimestamp().longValue());
  }
}
