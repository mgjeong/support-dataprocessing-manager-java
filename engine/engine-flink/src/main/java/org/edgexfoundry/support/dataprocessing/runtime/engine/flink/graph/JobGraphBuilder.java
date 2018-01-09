package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.DataFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TaskFormat;

public class JobGraphBuilder {
  private Config config;

  private Map<Vertex, List<Vertex>> edges;

  public JobGraph getInstance(StreamExecutionEnvironment env,
      Reader jsonConfig) throws Exception {

    if (env == null || jsonConfig == null) {
      throw new RuntimeException("Failed to set execution environment");
    }
    buildConfig(jsonConfig);
    initConfig(env);

    return new JobGraph(env, edges);
  }

  private void initConfig(StreamExecutionEnvironment env) throws Exception{
    if (this.config == null) {
      throw new RuntimeException("Job configuration is null");
    }
    HashMap<Integer, Vertex> map = new HashMap<>();
    for (DataFormat sourceFormat : config.getSources()) {
      SourceVertex source = new SourceVertex(env, sourceFormat);
      map.put(source.getId(), source);
    }

    for (DataFormat sinkFormat : config.getSinks()) {
      SinkVertex sink = new SinkVertex(sinkFormat);
      map.put(sink.getId(), sink);
    }

    for (TaskFormat taskFormat : config.getTasks()) {
      FlatMapTaskVertex task = new FlatMapTaskVertex(taskFormat);
      map.put(task.getId(), task);
    }

    edges = new HashMap<>();
    for (Edge edge : config.getEdges()) {
      int from = edge.getFrom();
      int to = edge.getTo();
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

  private void buildConfig(Reader jsonConfig) throws Exception {
    this.config = new Gson().fromJson(jsonConfig, Config.class);
    jsonConfig.close();
  }

}
