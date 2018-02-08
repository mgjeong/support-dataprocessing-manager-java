package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEdge;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSink;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSource;

public class ScriptGraphBuilder {

  private WorkflowData jobConfig;

  private Map<ScriptVertex, List<ScriptVertex>> edges;

  /**
   * Builds a graph corresponding to the given workflow.
   * From {@link WorkflowData}, this builder will generate the connection between vertices,
   * which consists of sources, sinks, and processors.
   *
   * @param workflowData Composition of sources, sinks, processors, and their connections
   * @return Script graph to decide reading sequence and to prepare Kapacitor script
   */
  public ScriptGraph getInstance(WorkflowData workflowData) {
    this.jobConfig = workflowData;
    initConfig();

    return new ScriptGraph(jobConfig.getWorkflowName(), edges);
  }

  private void initConfig() {
    if (jobConfig == null) {
      throw new NullPointerException("Job configuration is null");
    }

    if (isEmpty(jobConfig.getSources()) || isEmpty(jobConfig.getSinks())) {
      throw new NullPointerException("Source and sink must be specified");
    }

    Map<Long, ScriptVertex> map = new HashMap<>();
    for (WorkflowSource injectInfo : jobConfig.getSources()) {
      InjectVertex injector = new InjectVertex(injectInfo);
      map.put(injectInfo.getId(), injector);
    }

    for (WorkflowSink deliverInfo : jobConfig.getSinks()) {
      DeliverVertex deliverer = new DeliverVertex(deliverInfo);
      map.put(deliverInfo.getId(), deliverer);
    }

    for (WorkflowProcessor queryInfo : jobConfig.getProcessors()) {
      QueryVertex query = new QueryVertex(queryInfo);
      map.put(queryInfo.getId(), query);
    }

    edges = new HashMap<>();
    for (WorkflowEdge edge : jobConfig.getEdges()) {
      Long from = edge.getFromId();
      Long to = edge.getToId();
      if (map.containsKey(from) && map.containsKey(to)) {
        ScriptVertex fromVertex = map.get(from);
        ScriptVertex toVertex = map.get(to);
        if (!edges.containsKey(fromVertex)) {
          List<ScriptVertex> toes = new ArrayList<>();
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