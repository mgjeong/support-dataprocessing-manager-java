package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script.graph.QueryVertex;
import org.junit.Assert;
import org.junit.Test;

public class QueryVertexTest {

  private static final String TEST_QUERY = "ma(<BC>, 5)";

  @Test
  public void testGetId() {
    WorkflowProcessor processor = new WorkflowProcessor();
    processor.setId(3L);
    QueryVertex vertex = new QueryVertex(processor);
    Assert.assertEquals(vertex.getId(), (Long) 3L);
  }

  @Test
  public void testGetScript() {
    WorkflowProcessor processor = new WorkflowProcessor();
    processor.setId(3L);

    QueryVertex vertex = new QueryVertex(processor);

    try {
      vertex.getScript();
    } catch (IllegalStateException e) {
      processor.setName("query");
      processor.getConfig().put("request", 1);

      try {
        vertex.getScript();
      } catch (RuntimeException e1) {
        return;
      }
    }

    Assert.fail("Expected exceptions did not occur");
  }

  @Test
  public void testGetScriptWithInvalidScript() {
    WorkflowProcessor processor = new WorkflowProcessor();
    processor.setId(3L);
    processor.setName("query");
    processor.getConfig().put("request", TEST_QUERY);

    QueryVertex vertex = new QueryVertex(processor);
    vertex.getScript();
  }
}
