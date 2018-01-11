package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor;

import com.google.gson.Gson;
import java.io.FileReader;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.JobResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyData;
import org.edgexfoundry.support.dataprocessing.runtime.engine.Engine;
import org.junit.Assert;
import org.junit.Test;

public class KapacitorEngineTest {
  private static final String testConfigPath = "src/test/resources/config_kapacitor.json";

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

  @Test
  public void testDeployTopology() {
    try {
      TopologyData sampleTopology = getSampleTopology();
      Engine en = new KapacitorEngine("localhost", 9092);
      String id = en.createJob(sampleTopology);
      en.deploy(id);
    } catch (Exception e) {
      Assert.fail();
    }
  }

  private TopologyData getSampleTopology() throws Exception {
    return new Gson().fromJson(new FileReader(testConfigPath), TopologyData.class);
  }
}
