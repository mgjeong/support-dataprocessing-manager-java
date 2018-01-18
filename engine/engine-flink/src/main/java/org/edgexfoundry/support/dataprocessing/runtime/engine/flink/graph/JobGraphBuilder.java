package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph;

import com.google.gson.Gson;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEdge;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologySink;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologySource;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.vertex.FlatMapTaskVertex;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.vertex.SinkVertex;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.vertex.SourceVertex;

public class JobGraphBuilder {
  private TopologyData jobConfig;

  private Map<Vertex, List<Vertex>> edges;

  public JobGraph getInstance(StreamExecutionEnvironment env,
      Reader jsonConfig) throws Exception {

    if (env == null || jsonConfig == null) {
      throw new RuntimeException("Failed to set execution environment");
    }
    readJson(jsonConfig);
    initConfig(env);

    return new JobGraph(jobConfig.getTopologyName(), edges);
  }

  private void initConfig(StreamExecutionEnvironment env) throws Exception {
    if (this.jobConfig == null) {
      throw new RuntimeException("Job configuration is null");
    }
    HashMap<Long, Vertex> map = new HashMap<>();
    for (TopologySource sourceInfo : jobConfig.getSources()) {
      SourceVertex source = new SourceVertex(env, sourceInfo);
      map.put(sourceInfo.getId(), source);
    }

    for (TopologySink sinkInfo : jobConfig.getSinks()) {
      SinkVertex sink = new SinkVertex(sinkInfo);
      map.put(sink.getId(), sink);
    }

    for (TopologyProcessor taskInfo : jobConfig.getProcessors()) {
      FlatMapTaskVertex task = new FlatMapTaskVertex(taskInfo);
      map.put(task.getId(), task);
    }

    edges = new HashMap<>();
    for (TopologyEdge edge : jobConfig.getEdges()) {
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
      }
    }

  }

  private void readJson(Reader jsonConfig) throws Exception {
    this.jobConfig = new Gson().fromJson(jsonConfig, TopologyData.class);
  }

}
