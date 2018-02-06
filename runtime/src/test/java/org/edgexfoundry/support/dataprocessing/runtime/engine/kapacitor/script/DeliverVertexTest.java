package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script.graph.DeliverVertex;
import org.junit.Assert;
import org.junit.Test;

public class DeliverVertexTest {

  @Test
  public void testGetId() {
    WorkflowSink dst = new WorkflowSink();
    dst.setId(3L);

    DeliverVertex vertex = new DeliverVertex(dst);
    Assert.assertEquals(vertex.getId(), (Long) 3L);
  }

  @Test
  public void testGetScript() {
    WorkflowSink dst = new WorkflowSink();
    dst.setId(3L);
    dst.getConfig().put("dataType", "ezmq");
    dst.getConfig().put("dataSink", "localhost:55555:location/machine/id");

    DeliverVertex vertex = new DeliverVertex(dst);
    Assert.assertNotNull(vertex.getScript());

    dst.getConfig().put("dataSink", "localhost:55555");
    Assert.assertNotNull(vertex.getScript());

    dst.getConfig().put("name", "anotherTopicName");
    Assert.assertNotNull(vertex.getScript());
  }
}
