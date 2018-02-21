package org.edgexfoundry.support.dataprocessing.runtime.engine.flink;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class FlinkJobOverviewTest {

  @Test
  public void testGetterAndSetter() {
    FlinkJobOverview overview = new FlinkJobOverview();
    List<FlinkJob> running = new ArrayList<>();
    List<FlinkJob> finished = new ArrayList<>();

    overview.setRunning(running);
    overview.setFinished(finished);

    Assert.assertEquals(running, overview.getRunning());
    Assert.assertEquals(finished, overview.getFinished());
  }
}
