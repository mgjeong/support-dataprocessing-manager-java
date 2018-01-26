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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptGraphBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(ScriptGraphBuilder.class);
  private WorkflowData jobConfig;

  private Map<ScriptVertex, List<ScriptVertex>> edges;

  public ScriptGraph getInstance(WorkflowData workflowData) throws Exception {
    this.jobConfig = workflowData;
    initConfig();

    return new ScriptGraph(jobConfig.getWorkflowName(), edges);
  }

  private void initConfig() throws Exception{
    if (this.jobConfig == null) {
      throw new RuntimeException("Job configuration is null");
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
      }
    }

  }

}
