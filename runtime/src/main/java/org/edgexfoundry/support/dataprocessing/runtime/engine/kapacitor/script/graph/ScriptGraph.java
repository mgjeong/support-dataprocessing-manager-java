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

  public ScriptGraph initialize() {
    this.processingOrder = TopologicalSort();
    return this;
  }

  private List<ScriptVertex> TopologicalSort() {
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
      throw new IllegalStateException("Cycle");
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

  public String generateScript() throws Exception {
    String jobScript = "";
    for (ScriptVertex vertex : processingOrder) {
      jobScript += vertex.getScript() + "\n";
    }

    return jobScript;
  }

}
