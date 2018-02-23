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
package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponent;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEdge;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSink;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class JobGraphBuilderTest {

  private static long count = 1;
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  StreamExecutionEnvironment env = Mockito.mock(StreamExecutionEnvironment.class);

  @Test
  public void testGetInstanceWithNullEnvironment() throws Exception {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("environment");
    new JobGraphBuilder().getInstance(null, null);
  }

  @Test
  public void testGetInstanceWithNullWorkflow() throws Exception {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("job");
    new JobGraphBuilder().getInstance(env, null);
  }

  @Before
  public void resetCount() {
    count = 1;
  }

  private <T extends WorkflowComponent> List<T> getComponent(int length, Class<T> clazz)
      throws Exception {
    List<T> lists = new ArrayList<>();
    for (int i = 0; i < length; i++) {
      T component = clazz.newInstance();
      component.setId(count++);
      lists.add(component);
    }

    return lists;
  }

  @Test
  public void testGetInstanceWithEmptySource() throws Exception {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("source");
    WorkflowData data = new WorkflowData();
    List<WorkflowSource> sources = new ArrayList<>();
    data.setSources(sources);
    new JobGraphBuilder().getInstance(env, data);
  }

  @Test
  public void testGetInstanceWithEmptyProcessors() throws Exception {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("task");
    WorkflowData data = new WorkflowData();
    data.setSources(getComponent(1, WorkflowSource.class));
    data.setProcessors(new ArrayList<>());
    data.setSinks(getComponent(1, WorkflowSink.class));
    new JobGraphBuilder().getInstance(env, data);
  }

  @Test
  public void testGetInstanceWithEmptySinks() throws Exception {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("sink");
    WorkflowData data = new WorkflowData();
    data.setSources(getComponent(1, WorkflowSource.class));
    data.setProcessors(getComponent(1, WorkflowProcessor.class));
    data.setSinks(new ArrayList<>());
    new JobGraphBuilder().getInstance(env, data);
  }

  @Test(expected = IllegalStateException.class)
  public void testWithInvalidEdges() throws Exception {
    WorkflowData data = new WorkflowData();
    data.setSources(getComponent(1, WorkflowSource.class));
    data.setProcessors(getComponent(1, WorkflowProcessor.class));
    data.setSinks(getComponent(1, WorkflowSink.class));

    WorkflowEdge invalidEdge = new WorkflowEdge();
    invalidEdge.setFromId(125L);
    invalidEdge.setToId(126L);
    List<WorkflowEdge> edges = new ArrayList<>();
    edges.add(invalidEdge);
    data.setEdges(edges);

    new JobGraphBuilder().getInstance(env, data);
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

    JobGraph jobGraph = new JobGraphBuilder().getInstance(env, data);
    Field m = JobGraph.class.getDeclaredField("edges");
    m.setAccessible(true);
    Map<Vertex, List<Vertex>> edgeMap = (Map<Vertex, List<Vertex>>) m.get(jobGraph);
    Assert.assertEquals(edgeMap.size(), 3);

    int total = 0;
    for (Vertex vertex : edgeMap.keySet()) {
      total += edgeMap.get(vertex).size();
    }
    Assert.assertEquals(total, 4);
  }

}
