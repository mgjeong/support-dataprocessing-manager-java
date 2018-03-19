package org.edgexfoundry.support.dataprocessing.runtime.engine.flink;

import org.junit.Assert;
import org.junit.Test;

public class FlinkTaskTest {

  @Test
  public void testGetterAndSetter() {
    FlinkTask task = new FlinkTask();
    task.setTotal(0);
    task.setPending(1);
    task.setRunning(2);
    task.setFinished(3);
    task.setCanceling(4);
    task.setCanceled(5);
    task.setFailed(6);

    Assert.assertEquals(0, task.getTotal());
    Assert.assertEquals(1, task.getPending());
    Assert.assertEquals(2, task.getRunning());
    Assert.assertEquals(3, task.getFinished());
    Assert.assertEquals(4, task.getCanceling());
    Assert.assertEquals(5, task.getCanceled());
    Assert.assertEquals(6, task.getFailed());
  }
}
