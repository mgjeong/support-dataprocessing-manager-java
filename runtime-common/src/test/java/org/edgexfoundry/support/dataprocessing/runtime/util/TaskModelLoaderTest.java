package org.edgexfoundry.support.dataprocessing.runtime.util;


import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;

import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModel;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModelParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.junit.Assert;
import org.junit.Test;


public class TaskModelLoaderTest {

  @Test
  public void testSampleTaskModel() throws Exception {
    ClassLoader classLoader = this.getClass().getClassLoader();

    TaskModelLoader loader = spy(new TaskModelLoader(null, classLoader));

    // valid task model
    doReturn(SampleTaskModel.class)
        .when(loader).getClassInstance("SampleTaskModel");

    // valid task model
    TaskModel tm = loader.newInstance("SampleTaskModel");
    Assert.assertNotNull(tm);
    Assert.assertEquals(SampleTaskModel.class.getSimpleName(), tm.getName());

    // abstract class
    doReturn(SampleAbstractTaskModel.class)
        .when(loader).getClassInstance("SampleAbstractTaskModel");
    tm = loader.newInstance("SampleAbstractTaskModel");
    Assert.assertNull(tm);

    // not an instance of task model
    doReturn(TaskModelLoaderTest.class)
        .when(loader).getClassInstance("TaskModelLoaderTest");
    tm = loader.newInstance("TaskModelLoaderTest");
    Assert.assertNull(tm);

  }

  @Test
  public void testInvalidTaskModel() {
    try {
      TaskModelLoader loader = new TaskModelLoader("invalidpath",
          this.getClass().getClassLoader());
      loader.newInstance("invalid class");
      Assert.fail("Should not reach here");
    } catch (Exception e) {
      // Success
    }
  }

  public static abstract class SampleAbstractTaskModel implements TaskModel {

  }

  // public for test purpose
  public static class SampleTaskModel implements TaskModel {

    @Override
    public TaskType getType() {
      return TaskType.REGRESSION;
    }

    @Override
    public String getName() {
      return SampleTaskModel.class.getSimpleName();
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
}
