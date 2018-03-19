package org.edgexfoundry.support.dataprocessing.runtime.engine.flink;

import org.junit.Assert;
import org.junit.Test;

public class FlinkJobTest {

  @Test
  public void testGetterAndSetter() {
    FlinkJob job = new FlinkJob();
    job.setJid("jid");
    job.setName("name");
    job.setState("running");
    long startTime = System.currentTimeMillis();
    job.setStarttime(startTime);
    long endTime = System.currentTimeMillis();
    job.setEndtime(endTime);
    long duration = 0;
    job.setDuration(duration);
    long lastModified = 0;
    job.setLastModification(lastModified);
    FlinkTask task = new FlinkTask();
    job.setTasks(task);

    Assert.assertEquals("jid", job.getJid());
    Assert.assertEquals("name", job.getName());
    Assert.assertEquals("running", job.getState());
    Assert.assertEquals(startTime, job.getStarttime(), 0);
    Assert.assertEquals(endTime, job.getEndtime(), 0);
    Assert.assertEquals(duration, job.getDuration(), 0);
    Assert.assertEquals(lastModified, job.getLastModification(), 0);
    Assert.assertEquals(task, job.getTasks());
  }
}
