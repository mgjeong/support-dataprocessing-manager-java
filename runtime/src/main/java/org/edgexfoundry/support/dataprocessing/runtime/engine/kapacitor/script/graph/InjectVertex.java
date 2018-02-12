package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script.graph;

import java.util.HashMap;
import java.util.Map;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSource;

public class InjectVertex implements ScriptVertex {
  WorkflowSource config;

  public InjectVertex(WorkflowSource config) {
    this.config = config;
  }

  @Override
  public Long getId() {
    return this.config.getId();
  }

  @Override
  public String getScript() {
    Map<String, Object> properties = this.config.getConfig().getProperties();
    String dataType = ((String) properties.get("dataType")).toLowerCase();
    String dataSource = ((String) properties.get("dataSource")).replaceAll("\\s", "");

    if (!dataType.equals("ezmq")) {
      throw new RuntimeException("Unsupported input data type; " + dataType);
    }
    if (dataSource == null) {
      throw new RuntimeException("Invalid data source; " + dataSource);
    }

    String[] sourceSplits = dataSource.split(":", 3);
    String[] topics = null;
    if (sourceSplits.length == 3) {
      topics = sourceSplits[2].split(",");
    }

    String sourceAddress = sourceSplits[0] + ':' + sourceSplits[1];
    Map<String, String> scriptHeaders = new HashMap<>();

    for (String topic : topics) {
      String key = topic;
      scriptHeaders.put(key, generateScriptHeaderByTopic(topic, sourceAddress, topic));
    }

    if (topics == null) {
      String value = generateScriptHeaderByTopic("v" + sourceAddress, sourceAddress, null);
      scriptHeaders.put(sourceAddress, value);
    }

    String script = "";
    for (String header : scriptHeaders.keySet()) {
      script += scriptHeaders.get(header) + '\n';
    }

    return script;
  }

  private String generateScriptHeaderByTopic(String table, String sourceAddress, String topic) {
    String validName = table.replaceAll("\\W", "");
    String measurement =
        String.format("var %s = stream|from().measurement(\'%s\')", validName, validName);
    String injection = String.format("@inject().source('ezmq').address(\'%s\')", sourceAddress);
    injection += String.format(".into(\'%s\')", validName);

    if (topic == null) {
      return measurement + injection;
    }

    String subscription = String.format(".topic(\'%s\')", topic);
    return measurement + injection + subscription;
  }
}
