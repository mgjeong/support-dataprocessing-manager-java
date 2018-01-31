package org.edgexfoundry.support.dataprocessing.runtime.task;

import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskParam.UiFieldType;

public class SimpleTaskModelTest implements TaskModel {

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
    return SimpleTaskModelTest.class.getSimpleName();
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
