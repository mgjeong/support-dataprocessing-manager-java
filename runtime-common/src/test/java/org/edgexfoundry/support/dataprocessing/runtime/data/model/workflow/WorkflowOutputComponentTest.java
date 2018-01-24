package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

public class WorkflowOutputComponentTest {

  @Test
  public void testSetterAndGetter() {
    WorkflowOutputComponent component = new WorkflowSource();
    component.setOutputStreams(new ArrayList<>());
    Assert.assertEquals(0, component.getOutputStreams().size());

    WorkflowStream stream = new WorkflowStream();
    component.addOutputStream(stream);
    Assert.assertEquals(1, component.getOutputStreams().size());
  }

  @Test
  public void testInvalidSetter() {
    WorkflowOutputComponent component = new WorkflowSource();
    try {
      component.setOutputStreams(null);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }

    try {
      component.addOutputStream(null);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }
  }
}
