package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script.graph;

import java.util.Map;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryVertex implements ScriptVertex{
  final static Logger LOGGER = LoggerFactory.getLogger(QueryVertex.class);
  WorkflowProcessor config;

  public QueryVertex(WorkflowProcessor config) {
    this.config = config;
  }

  @Override
  public Long getId() {
    return this.config.getId();
  }

  @Override
  public String getScript() {
    if (!this.config.getName().equalsIgnoreCase("query")) {
      LOGGER.error("Cannot handle types except query");
    }
    Map<String, Object> properties = this.config.getConfig().getProperties();
    StringBuilder builder = new StringBuilder();
    builder.append("var id");
    builder.append(getId());
    builder.append('=');

    Object scriptBodyObject = properties.get("request");
    if (scriptBodyObject instanceof String) {
      builder.append(((String) scriptBodyObject).replace("<", "\'").replace(">", "\'") + "\n");
    } else {
      throw new RuntimeException("Request should be String type");
    }
    return builder.toString();
  }
}