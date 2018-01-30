package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.task;

import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModel;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModelParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TestModel implements TaskModel {
  @Override
  public String getName() {
    return TestModel.class.getName();
  }

  @Override
  public TaskType getType() {
    return TaskType.INVALID;
  }

  @Override
  public void setParam(TaskModelParam param) {
  }

  @Override
  public DataSet calculate(DataSet in) {
    return in;
  }

  @Override
  public void setInRecordKeys(List<String> inRecordKeys) {
  }

  @Override
  public void setOutRecordKeys(List<String> outRecordKeys) {
  }
}
