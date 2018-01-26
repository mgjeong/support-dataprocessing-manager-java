package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.task;

import java.util.List;
import java.util.Map;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModel;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModelParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlatMapTask extends RichFlatMapFunction<DataSet, DataSet> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlatMapTask.class);

  private TaskModel task;

  private Map<String, Object> properties;

  public FlatMapTask(Map<String, Object> properties) {
    this.properties = properties;
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);

    // Create task using TaskFactory which is made up by factory pattern.
    String jarPath = (String) properties.get("jar");
    ClassLoader classLoader = getRuntimeContext().getUserCodeClassLoader();
    String targetClass = (String) properties.get("className");
    this.task = new ModelLoader(jarPath, classLoader).newInstance(targetClass);
    if (this.task != null) {
      TaskModelParam taskModelParam = new TaskModelParam();
      for (Map.Entry<String, Object> entry : properties.entrySet()) {
        createTaskModelParam(taskModelParam, entry.getKey(), entry.getValue());
      }

      this.task.setParam(taskModelParam);
      this.task.setInRecordKeys((List<String>) properties.get("inrecord"));
      this.task.setOutRecordKeys((List<String>) properties.get("outrecord"));

      LOGGER.debug("{} is loaded as a FlatMap task ", this.task.getName());
    }
  }

  private void createTaskModelParam(TaskModelParam parent, String key, Object value) {
    String[] tokens = key.split("/", 1);
    if (tokens.length == 1) { // terminal case
      parent.put(key, value);
      return;
    }

    // recurse
    TaskModelParam child = new TaskModelParam();
    parent.put(tokens[0], child);
    createTaskModelParam(child, tokens[1], value);
  }

  @Override
  public void flatMap(DataSet dataSet, Collector<DataSet> collector) throws Exception {
    dataSet = this.task.calculate(dataSet);
    if (dataSet != null) {
      collector.collect(dataSet);
    }
  }
}
