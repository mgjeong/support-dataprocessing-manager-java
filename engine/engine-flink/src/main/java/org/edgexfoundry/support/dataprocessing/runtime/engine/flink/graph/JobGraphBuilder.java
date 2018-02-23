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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEdge;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSink;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSource;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.vertex.FlatMapTaskVertex;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.vertex.SinkVertex;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.vertex.SourceVertex;

public class JobGraphBuilder {

  private WorkflowData jobConfig;

  private Map<Vertex, List<Vertex>> edges;

  /**
   * Returns JobGraph objects that corresponds to the workflow
   * The env argument must specify Flink execution environment on the machine, and the workflowData
   * argument contains sources, sinks, processors and their connections.
   * This method returns JobGraph object only when the given workflow is valid. When the workflow is
   * not suitable for drawing the job graph from source to sink and at least one processor, this
   * method throws an exception.
   *
   * @param env Flink execution environment
   * @param workflowData Workflow
   * @return JobGraph object that can initiate Flink job
   * @throws Exception If Flink execution environment is missing, source/sink/processor is missing,
   *        or unreachable edge exists.
   */
  public JobGraph getInstance(StreamExecutionEnvironment env,
      WorkflowData workflowData) throws Exception {

    if (env == null) {
      throw new NullPointerException("Invalid execution environment");
    }

    if (workflowData == null) {
      throw new NullPointerException("Null job configurations");
    }

    this.jobConfig = workflowData;
    initConfig(env);

    return new JobGraph(jobConfig.getWorkflowName(), edges);
  }

  private void initConfig(StreamExecutionEnvironment env) {
    if (isEmpty(jobConfig.getSources())) {
      throw new NullPointerException("Empty source information");
    }

    if (isEmpty(jobConfig.getSinks())) {
      throw new NullPointerException("Empty sink information");
    }

    if (isEmpty(jobConfig.getProcessors())) {
      throw new NullPointerException("Empty task information");
    }

    HashMap<Long, Vertex> map = new HashMap<>();
    for (WorkflowSource sourceInfo : jobConfig.getSources()) {
      SourceVertex source = new SourceVertex(env, sourceInfo);
      map.put(sourceInfo.getId(), source);
    }

    for (WorkflowSink sinkInfo : jobConfig.getSinks()) {
      SinkVertex sink = new SinkVertex(sinkInfo);
      map.put(sink.getId(), sink);
    }

    for (WorkflowProcessor taskInfo : jobConfig.getProcessors()) {
      FlatMapTaskVertex task = new FlatMapTaskVertex(taskInfo);
      map.put(task.getId(), task);
    }

    edges = new HashMap<>();
    for (WorkflowEdge edge : jobConfig.getEdges()) {
      Long from = edge.getFromId();
      Long to = edge.getToId();
      if (map.containsKey(from) && map.containsKey(to)) {
        Vertex fromVertex = map.get(from);
        Vertex toVertex = map.get(to);
        if (!edges.containsKey(fromVertex)) {
          List<Vertex> toes = new ArrayList<>();
          toes.add(toVertex);
          edges.put(fromVertex, toes);
        } else {
          edges.get(fromVertex).add(toVertex);
        }
      } else {
        throw new IllegalStateException("Unavailable vertex included when " + from + "->" + to);
      }
    }
  }

  private <T> boolean isEmpty(List<T> list) {
    return list == null || list.isEmpty();
  }
}
