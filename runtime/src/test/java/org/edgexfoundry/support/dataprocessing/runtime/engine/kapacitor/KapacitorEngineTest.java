package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.JobResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.engine.Engine;
import org.junit.Assert;
import org.junit.Test;

public class KapacitorEngineTest {
  @Test
  public void testKapacitorEngine() {
    Engine engine = new KapacitorEngine("localhost", 9092);

    JobResponseFormat format = engine.createJob();
    Assert.assertNotNull(format);

    format = engine.createJob("testjob");
    Assert.assertNotNull(format);
  }

  @Test
  public void testRun() {
    Engine engine = new KapacitorEngine("localhost", 9092);

    JobResponseFormat format = engine.createJob();
    Assert.assertNotNull(format);

    engine.run(format.getJobId());
    engine.stop(format.getJobId());
    engine.delete(format.getJobId());
  }
}
