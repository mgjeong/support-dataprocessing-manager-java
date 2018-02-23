package org.edgexfoundry.support.dataprocessing.runtime.task;

import org.edgexfoundry.support.dataprocessing.runtime.task.TaskParam.UiFieldType;
import org.junit.Test;

public class TaskParamTest {

  @Test
  public void testUiFieldType() {
    for (UiFieldType uiFieldType : UiFieldType.values()) {
      System.out.println(uiFieldType.getUiFieldTypeText());
    }
  }

}
