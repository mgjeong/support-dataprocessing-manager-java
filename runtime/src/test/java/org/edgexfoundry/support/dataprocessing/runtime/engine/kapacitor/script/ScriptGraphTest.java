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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script.graph.ScriptGraph;
import org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script.graph.ScriptVertex;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ScriptGraphTest {

  private static final String ID = "testId";
  private static long count;

  @Before
  public void resetCount() {
    count = 1;
  }

  @Test
  public void testSetJobID() {
    String anotherId = "newId";
    ScriptGraph scriptGraph = new ScriptGraph(ID, null);
    scriptGraph.setJobId(anotherId);
    Assert.assertEquals(scriptGraph.getJobId(), anotherId);
  }

  @Test
  public void testGetJobId() {
    ScriptGraph scriptGraph = new ScriptGraph(ID, null);
    Assert.assertEquals(scriptGraph.getJobId(), ID);
  }

  @Test(expected = IllegalStateException.class)
  public void testInitializeWithCycle() {
    Map<ScriptVertex, List<ScriptVertex>> edges = new HashMap<>();
    List<ScriptVertex> vertices = new ArrayList<>();
    vertices.add(new TestVertex(count++));
    vertices.add(new TestVertex(count++));
    vertices.add(new TestVertex(count++));

    List<ScriptVertex> list = new ArrayList<>();
    list.add(vertices.get(1));
    edges.put(vertices.get(0), list);

    list = new ArrayList<>();
    list.add(vertices.get(2));
    edges.put(vertices.get(1), list);

    list = new ArrayList<>();
    list.add(vertices.get(0));
    edges.put(vertices.get(2), list);

    new ScriptGraph(ID, edges).initialize();
  }

  @Test
  public void testInitExecution() throws Exception {
    Map<ScriptVertex, List<ScriptVertex>> edges = new HashMap<>();
    List<ScriptVertex> vertices = new ArrayList<>();
    vertices.add(new TestVertex(count++));
    vertices.add(new TestVertex(count++));
    vertices.add(new TestVertex(count++));

    List<ScriptVertex> list = new ArrayList<>();
    list.add(vertices.get(1));
    edges.put(vertices.get(0), list);

    list = new ArrayList<>();
    list.add(vertices.get(2));
    edges.put(vertices.get(1), list);

    ScriptGraph scriptGraph = new ScriptGraph(ID, edges);
    scriptGraph.initialize();
    String answer = scriptGraph.generateScript();
    Assert.assertNotNull(answer);
    Assert.assertNotEquals(answer, "");
  }

  private final class TestVertex implements ScriptVertex {

    private Long id;

    public TestVertex(Long id) {
      this.id = id;
    }

    @Override
    public Long getId() {
      return id;
    }

    @Override
    public String getScript() {
      return "var test = stream|from()|ma('BC',5)";
    }
  }
}
