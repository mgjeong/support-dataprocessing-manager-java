package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.DataFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TaskFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(ScriptFactory.class);
  List<DataFormat> inputs = null;
  List<DataFormat> outputs = null;
  List<TaskFormat> tasks = null;


  public String getScript() throws RuntimeException, IOException {
    // Setting script
    if (inputs == null || outputs == null || tasks == null) {
      throw new RuntimeException("Invalid job information");
    }

    Map<String, String> headers = new HashMap<>();
    for (DataFormat input : inputs) {
      headers.putAll(generateScriptHeaders(input));
    }

    String scriptTail = "";
    for (DataFormat output : outputs) {
      scriptTail += generateScriptTail(output);
    }

    // This "query checking" logic should apply to task.getType() later
    // Also this current logic will check name is custom or not

    String scriptBody = "";
    for (TaskFormat task : tasks) {
      if (!task.getName().equals("query")) {
        throw new RuntimeException("For now, only one query type task is supported");
      }

      Object scriptBodyObject = task.getParams().get("request");
      if (scriptBodyObject instanceof String) {
        scriptBody += scriptBodyObject + "\n";
      } else {
        throw new RuntimeException("Request should be String type");
      }
    }

    String script = "";
    for (String header : headers.keySet()) {
      script += headers.get(header) + '\n';
    }

    if (scriptBody != null) {
      script += scriptBody.replace("<", "\'").replace(">", "\'") + '\n';
    }

    script += scriptTail;
    return script;
  }

  private Map<String, String> generateScriptHeaders(DataFormat source) {
    String dataType = source.getDataType().toLowerCase();
    String dataSource = source.getDataSource().replaceAll("\\s", "");

    if (!dataType.equals("emf")) {
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

    return scriptHeaders;
  }

  private String generateScriptHeaderByTopic(String table, String sourceAddress, String topic) {
    String validName = table.replaceAll("\\W", "");
    String measurement =
        String.format("var %s = stream|from().measurement(\'%s\')", validName, validName);
    String injection = String.format("@inject().source('emf').address(\'%s\')", sourceAddress);
    injection += String.format(".into(\'%s\')", validName);

    if (topic == null) {
      return measurement + injection;
    }

    String subscription = String.format(".topic(\'%s\')", topic);
    return measurement + injection + subscription;
  }

  private String generateScriptTail(DataFormat output) {
    String dataType = output.getDataType().toLowerCase();
    String dataSink = output.getDataSource().replaceAll("\\s", "");

    if (!dataType.equals("emf") && !dataType.equals("f") && !dataType.equals("mongodb")) {
      throw new RuntimeException("Unsupported output data type" + dataType);
    }
    String[] sinkSplits = dataSink.split(":", 3);
    String[] topics = null;

    String[] names = null;

    if (sinkSplits.length == 3) {
      topics = sinkSplits[2].split(",");
    }

    String sinkAddress = sinkSplits[0] + ':' + sinkSplits[1];
    if (output.getName() != null) {
      names = output.getName().trim().split(",");
    }

    if (names != null) {
      String result = "";
      for (String name : names) {
        result += generateScriptTailByTopic(dataType, sinkAddress, name);
      }
      return result;
    }

    if (topics == null) {
      return generateScriptHeaderByTopic(dataType, sinkAddress, null);
    } else {
      String scriptTail = "";
      for (String topic : topics) {
        scriptTail += generateScriptTailByTopic(dataType, sinkAddress, topic);
      }
      return scriptTail;
    }
  }

  private String generateScriptTailByTopic(String dataType, String dataSink, String topic) {
    String scriptTail =
        String.format("@deliver().sink(\'%s\').address(\'%s\')", dataType, dataSink);
    if (topic != null) {
      scriptTail += String.format(".topic(\'%s\')", topic) + '\n';
    }
    return scriptTail;
  }

  public void setInputs(List<DataFormat> inputs) {
    this.inputs = inputs;
  }

  public void setOutputs(List<DataFormat> outputs) {
    this.outputs = outputs;
  }

  public void setTasks(List<TaskFormat> tasks) {
    this.tasks = tasks;
  }

}
