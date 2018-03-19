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
package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptGraph {
  private String jobId;
  private List<ScriptVertex> processingOrder;
  private Map<ScriptVertex, List<ScriptVertex>> edges;

  private enum State {
    VISITED,
    UNVISITED
  }

  public ScriptGraph(String jobId, Map<ScriptVertex, List<ScriptVertex>> edges) {
    this.jobId = jobId;
    this.edges = edges;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public void initialize() {
    this.processingOrder = topologicalSort();
  }

  private List<ScriptVertex> topologicalSort() {
    Map<ScriptVertex, State> state = new HashMap<>();
    List<ScriptVertex> res = new ArrayList<>();
    for (ScriptVertex component : edges.keySet()) {
      if (state.get(component) != State.VISITED) {
        res.addAll(dfs(component, state));
      }
    }
    Collections.reverse(res);
    return res;
  }

  private List<ScriptVertex> dfs(ScriptVertex current, Map<ScriptVertex, State> state) {
    List<ScriptVertex> res = new ArrayList<>();
    if (state.get(current) == State.UNVISITED) {
      throw new IllegalStateException("Cycle is detected. Failed to compose job graph");
    }
    state.put(current, State.UNVISITED);
    List<ScriptVertex> adjVertices = adjacent(current);
    if (adjVertices != null) {
      for (ScriptVertex adj : adjacent(current)) {
        if (state.get(adj) != State.VISITED) {
          res.addAll(dfs(adj, state));
        }
      }
    }
    state.put(current, State.VISITED);
    res.add(current);
    return res;
  }

  private List<ScriptVertex> adjacent(ScriptVertex current) {
    return edges.get(current);
  }

  /**
   * Compose scripts from the graph and generate Kapacitor script.
   * Automatically it uses user-defined functions in Kapacitor for EdgeX message framework
   * when it comes to sourcing and sinking.
   * @return Kapacitor script from the given graph
   */
  public String generateScript() {
    StringBuffer jobScript = new StringBuffer();
    for (ScriptVertex vertex : processingOrder) {
      jobScript.append(vertex.getScript() + "\n");
    }

    return jobScript.toString();
  }

}
