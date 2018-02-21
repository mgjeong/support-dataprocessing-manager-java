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
