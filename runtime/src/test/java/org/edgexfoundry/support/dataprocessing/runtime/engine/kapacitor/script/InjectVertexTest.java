package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSource;
import org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script.graph.InjectVertex;
import org.junit.Assert;
import org.junit.Test;

public class InjectVertexTest {
  @Test
  public void testGetId() {
    WorkflowSource src = new WorkflowSource();
    src.setId(3L);

    InjectVertex vertex = new InjectVertex(src);
    Assert.assertEquals(vertex.getId(), (Long) 3L);
  }

  @Test
  public void testGetScript() {
    WorkflowSource src = new WorkflowSource();
    src.setId(3L);
    src.getConfig().put("dataType", "ezmq");
    src.getConfig().put("dataSource", "localhost:55555:location/machine/id");

    InjectVertex vertex = new InjectVertex(src);
    Assert.assertNotNull(vertex.getScript());

    src.getConfig().put("dataSource", "localhost:55555");
    vertex.getScript();
    Assert.assertNotNull(vertex.getScript());
  }
}
