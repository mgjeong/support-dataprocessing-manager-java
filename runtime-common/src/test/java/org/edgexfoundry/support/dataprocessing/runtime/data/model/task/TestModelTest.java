package org.edgexfoundry.support.dataprocessing.runtime.data.model.task;

import java.util.ArrayList;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TestModel.Gender;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModel;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModelParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.junit.Assert;
import org.junit.Test;

public class TestModelTest {

  @Test
  public void testConstructor() {
    TestModel testModel = new TestModel();
    Assert.assertTrue(testModel instanceof TaskModel);
  }

  @Test
  public void testSetterAndGetter() {
    TestModel testModel = new TestModel();
    testModel.setParam(new TaskModelParam());
    testModel.setInRecordKeys(new ArrayList<>());
    testModel.setOutRecordKeys(new ArrayList<>());
    Assert.assertEquals(TaskType.INVALID, testModel.getType());
    Assert.assertEquals(TestModel.class.getSimpleName(), testModel.getName());
    testModel.calculate(DataSet.create());

    Assert.assertEquals(2, Gender.values().length);
  }
}
