package org.edgexfoundry.support.dataprocessing.runtime.data.model.task;

import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModel;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModelParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskParam.UiFieldType;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;

/**
 * Class TestModel is provided only for unit tests.
 * Use this only when you want to ensure that your code works with arbitrary model.
 */
public final class TestModel implements TaskModel {

  @TaskParam(key = "age", uiType = UiFieldType.NUMBER, uiName = "Age")
  private int age;

  @TaskParam(key = "gender", uiType = UiFieldType.ENUMSTRING, uiName = "Gender")
  private Gender gender;

  public enum Gender {
    Male, Female
  }

  @Override
  public TaskType getType() {
    return TaskType.INVALID;
  }

  @Override
  public String getName() {
    return TestModel.class.getSimpleName();
  }

  @Override
  public void setParam(TaskModelParam param) {

  }

  @Override
  public void setInRecordKeys(List<String> inRecordKeys) {

  }

  @Override
  public void setOutRecordKeys(List<String> outRecordKeys) {

  }

  @Override
  public DataSet calculate(DataSet in) {
    return in;
  }
}
