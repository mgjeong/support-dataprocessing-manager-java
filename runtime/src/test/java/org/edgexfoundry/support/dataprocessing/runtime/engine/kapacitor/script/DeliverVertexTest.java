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
