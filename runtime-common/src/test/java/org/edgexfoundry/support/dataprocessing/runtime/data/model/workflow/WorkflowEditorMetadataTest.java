package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import org.junit.Assert;
import org.junit.Test;

public class WorkflowEditorMetadataTest {

  @Test
  public void testSetterAndGetter(){
    long now = System.currentTimeMillis();

    WorkflowEditorMetadata metadata = new WorkflowEditorMetadata();
    metadata.setWorkflowId(1L);
    metadata.setTimestamp(now);
    metadata.setData("{}");

    Assert.assertEquals(1L, metadata.getWorkflowId().longValue());
    Assert.assertEquals(now, metadata.getTimestamp().longValue());
    Assert.assertEquals("{}", metadata.getData());
  }

  @Test
  public void testInvalidSetter(){
    WorkflowEditorMetadata metadata = new WorkflowEditorMetadata();
    try{
      metadata.setWorkflowId(null);
      Assert.fail("Should not reach here.");
    } catch(RuntimeException e){
      // success
    }
  }
}
