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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponent;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEdge;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSink;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSource;
import org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script.graph.ScriptGraph;
import org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script.graph.ScriptGraphBuilder;
import org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script.graph.ScriptVertex;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ScriptGraphBuilderTest {

  private static long count = 1L;

  @Before
  public void resetCount() {
    count = 1L;
  }

  @Test(expected = NullPointerException.class)
  public void testWithNullConfig() {
    new ScriptGraphBuilder().getInstance(null);
  }

  @Test(expected = NullPointerException.class)
  public void testWithNullSourceSink() {
    new ScriptGraphBuilder().getInstance(new WorkflowData());
  }

  @Test(expected = IllegalStateException.class)
  public void testWithInvalidEdge() throws Exception {
    WorkflowData data = new WorkflowData();
    data.setSources(getComponent(1, WorkflowSource.class));
    data.setProcessors(getComponent(1, WorkflowProcessor.class));
    data.setSinks(getComponent(1, WorkflowSink.class));

    WorkflowEdge invalidEdge = new WorkflowEdge();
    invalidEdge.setFromId(76L);
    invalidEdge.setToId(0L);
    List<WorkflowEdge> edges = new ArrayList<>();
    edges.add(invalidEdge);
    data.setEdges(edges);

    new ScriptGraphBuilder().getInstance(data);
  }

  @Test
  public void testGetInstance() throws Exception {
    WorkflowData data = new WorkflowData();
    data.setSources(getComponent(1, WorkflowSource.class));
    data.setProcessors(getComponent(2, WorkflowProcessor.class));
    data.setSinks(getComponent(1, WorkflowSink.class));

    WorkflowEdge edge = new WorkflowEdge();
    edge.setFromId(1L);
    edge.setToId(2L);
    List<WorkflowEdge> edges = new ArrayList<>();
    edges.add(edge);

    edge = new WorkflowEdge();
    edge.setFromId(1L);
    edge.setToId(3L);
    edges.add(edge);
    data.setEdges(edges);

    edge = new WorkflowEdge();
    edge.setFromId(2L);
    edge.setToId(4L);
    edges.add(edge);
    data.setEdges(edges);

    edge = new WorkflowEdge();
    edge.setFromId(3L);
    edge.setToId(4L);
    edges.add(edge);
    data.setEdges(edges);

    ScriptGraph graph = new ScriptGraphBuilder().getInstance(data);
    Field m = ScriptGraph.class.getDeclaredField("edges");
    m.setAccessible(true);
    Map<ScriptVertex, List<ScriptVertex>> edgeMap =
        (Map<ScriptVertex, List<ScriptVertex>>) m.get(graph);
    Assert.assertEquals(edgeMap.size(), 3);

    int total = 0;
    for (ScriptVertex vertex : edgeMap.keySet()) {
      total += edgeMap.get(vertex).size();
    }
    Assert.assertEquals(total, 4);
  }

  private <T extends WorkflowComponent> List<T> getComponent(int length, Class<T> clazz)
      throws Exception {
    List<T> list = new ArrayList<>();
    for (int i = 0; i < length; i++) {
      T component = clazz.newInstance();
      component.setId(count++);
      list.add(component);
    }
    return list;
  }
}
