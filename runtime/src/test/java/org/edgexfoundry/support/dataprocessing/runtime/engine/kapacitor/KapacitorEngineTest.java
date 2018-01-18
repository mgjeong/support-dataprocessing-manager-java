package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor;

public class KapacitorEngineTest {

  private static final String testConfigPath = "src/test/resources/config_kapacitor.json";
/*
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
  public void testDeployWorkflow() {
    try {
      WorkflowData sampleWorkflow = getSampleWorkflow();
      Engine en = new KapacitorEngine("localhost", 9092);
      String id = en.createJob(sampleWorkflow);
      en.deploy(id);
    } catch (Exception e) {
      Assert.fail();
    }
  }

  private WorkflowData getSampleWorkflow() throws Exception {
    return new Gson().fromJson(new FileReader(testConfigPath), WorkflowData.class);
  }
  */
}
