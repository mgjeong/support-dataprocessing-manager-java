package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script.graph;

import java.util.Map;
import javax.management.Query;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryVertex implements ScriptVertex{
  final static Logger LOGGER = LoggerFactory.getLogger(QueryVertex.class);
  TopologyProcessor config;

  public QueryVertex(TopologyProcessor config) {
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
    String scriptBody = "";
    Object scriptBodyObject = properties.get("request");
    if (scriptBodyObject instanceof String) {
      scriptBody += scriptBodyObject + "\n";
    } else {
      throw new RuntimeException("Request should be String type");
    }
    return scriptBody;
  }
}
