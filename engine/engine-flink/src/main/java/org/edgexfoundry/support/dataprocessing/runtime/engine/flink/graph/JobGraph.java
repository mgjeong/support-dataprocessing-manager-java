package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;

public class JobGraph {
  private String jobId;
  private StreamExecutionEnvironment env;
  private List<Vertex> processingOrder;
  private Map<Vertex, List<Vertex>> edges;

  private enum State {
    VISITED,
    UNVISITED
  }

  public JobGraph(String jobId, StreamExecutionEnvironment env, Map<Vertex, List<Vertex>> edges) {
    this.jobId = jobId;
    this.env = env;
    this.edges = edges;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public JobGraph initialize() {
    this.processingOrder = TopologicalSort();
    return this;
  }

  private List<Vertex> TopologicalSort() {
    Map<Vertex, State> state = new HashMap<>();
    List<Vertex> res = new ArrayList<>();
    for (Vertex component : edges.keySet()) {
      if (state.get(component) != State.VISITED) {
        res.addAll(dfs(component, state));
      }
    }
    Collections.reverse(res);
    return res;
  }

  private List<Vertex> dfs(Vertex current, Map<Vertex, State> state) {
    List<Vertex> res = new ArrayList<>();
    if (state.get(current) == State.UNVISITED) {
      throw new IllegalStateException("Cycle");
    }
    state.put(current, State.UNVISITED);
    List<Vertex> adjVertices = adjacent(current);
    if (adjVertices != null) {
      for (Vertex adj : adjacent(current)) {
        if (state.get(adj) != State.VISITED) {
          res.addAll(dfs(adj, state));
        }
      }
    }
    state.put(current, State.VISITED);
    res.add(current);
    return res;
  }

  private List<Vertex> adjacent(Vertex current) {
    return edges.get(current);
  }


  public void execute() throws Exception {
    for (Vertex vertex : processingOrder) {
      DataStream<DataSet> outFlux = vertex.serve();
      List<Vertex> nextVertices = edges.get(vertex);
      if (nextVertices != null) {
        for (Vertex next : edges.get(vertex)) {
          next.setFluxIn(outFlux);
        }
      }
    }
    this.env.execute(this.jobId);
  }

}
