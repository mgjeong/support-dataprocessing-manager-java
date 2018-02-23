package org.edgexfoundry.support.dataprocessing.runtime.engine;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData.EngineType;
import org.junit.Assert;
import org.junit.Test;

public class EngineManagerTest {

  @Test
  public void testGetEngine() {
    Engine flinkEngine = EngineManager.getInstance().getEngine("localhost", 8081, EngineType.FLINK);
    Assert.assertNotNull(flinkEngine);
    Engine kapacitorEngine = EngineManager.getInstance()
        .getEngine("localhost", 8082, EngineType.KAPACITOR);
    Assert.assertNotNull(kapacitorEngine);
    Assert.assertTrue(flinkEngine != kapacitorEngine);

    Engine flinkDup = EngineManager.getInstance().getEngine("localhost", 8081, EngineType.FLINK);
    Assert.assertNotNull(flinkDup);
    Assert.assertTrue(flinkEngine == flinkDup);

    Engine flinkNew = EngineManager.getInstance().getEngine("localhost", 8082, EngineType.FLINK);
    Assert.assertNotNull(flinkNew);
    Assert.assertTrue(flinkEngine != flinkNew);
  }
}
