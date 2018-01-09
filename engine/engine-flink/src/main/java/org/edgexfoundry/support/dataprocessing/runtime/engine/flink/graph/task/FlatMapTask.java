package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.task;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TaskFormat;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModel;
import org.edgexfoundry.support.dataprocessing.runtime.util.TaskModelLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlatMapTask extends RichFlatMapFunction<DataSet, DataSet> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlatMapTask.class);

  private TaskModel task;

  private TaskFormat taskFormat;

  public FlatMapTask(TaskFormat taskFormat) {
    this.taskFormat = taskFormat;
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);

    LOGGER.info("Attempting to create task for {}", this.taskFormat.getName());
    // Create task using TaskFactory which is made up by factory pattern.
    String jarPath = this.taskFormat.getJar();
    ClassLoader classLoader = getRuntimeContext().getUserCodeClassLoader();
    String targetClass = this.taskFormat.getClassName();
    this.task = new TaskModelLoader(jarPath, classLoader).newInstance(targetClass);
  }

  @Override
  public void flatMap(DataSet dataSet, Collector<DataSet> collector) throws Exception {
    dataSet = this.task.calculate(dataSet);
    if (dataSet != null) {
      collector.collect(dataSet);
    }
  }
}
