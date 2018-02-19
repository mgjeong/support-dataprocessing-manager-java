/*******************************************************************************
 * Copyright 2018 Samsung Electronics All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
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
