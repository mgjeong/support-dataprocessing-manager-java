package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.DataFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TaskFormat;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModelParam;
import org.junit.Assert;
import org.junit.Test;

public class ScriptFactoryTest {
  private static final int IOLENGTH = 2;
  private static final int TASKLENGTH = 1;
  private static final String TYPEABNORMAL = "random";
  private static final String TYPENORMAL = "ezmq";
  private static final String ADDRESS = "localhost:55555:topic";
  private static final String TASKVALID = "query";
  private static final String TASKINVALID = "awful";
  private static final String REQUEST = "request";
  private static final String REQUESTMSG = "movingAverage(<BC>,1)";

  @Test (expected=RuntimeException.class)
  public void nullTest() {
    ScriptFactory scriptFactory = new ScriptFactory();
    try {
      scriptFactory.getScript();
    } catch (IOException e) {
      Assert.fail();
    }
    Assert.fail();
  }

  @Test
  public void testNormalScript() {
    List<DataFormat> inputs = getNormalData();
    List<DataFormat> outputs = getNormalData();
    List<TaskFormat> tasks = getNormalTask();
    ScriptFactory scriptFactory = new ScriptFactory();
    scriptFactory.setInputs(inputs);
    scriptFactory.setOutputs(outputs);
    scriptFactory.setTasks(tasks);

    try {
      scriptFactory.getScript();
    } catch (Exception e) {
      Assert.fail();
    }
  }

  @Test
  public void testAbnormalScript() {
    List<DataFormat> badInputs = getAbnormalData();
    List<DataFormat> badOutputs = getAbnormalData();
    List<TaskFormat> badTasks = getAbnormalTask();

    List<DataFormat> goodInputs = getNormalData();
    List<DataFormat> goodOutputs = getNormalData();

    ScriptFactory scriptFactory = new ScriptFactory();
    scriptFactory.setInputs(badInputs);
    scriptFactory.setOutputs(badOutputs);
    scriptFactory.setTasks(badTasks);

    try {
      scriptFactory.getScript();
    } catch (RuntimeException e1) {
      scriptFactory.setInputs(goodInputs);
      try {
        scriptFactory.getScript();
      } catch (RuntimeException e2) {
        scriptFactory.setOutputs(goodOutputs);
        try {
          scriptFactory.getScript();
        } catch (RuntimeException e3) {
          return;
        } catch (Exception e4) {
          Assert.fail();
        }
      } catch (Exception e5) {
        Assert.fail();
      }
    } catch (Exception e6) {
      Assert.fail();
    }
    Assert.fail();
  }

  private List<DataFormat> getAbnormalData() {
    return prepareData(TYPEABNORMAL, ADDRESS, null);
  }

  private List<DataFormat> getNormalData() {
    return prepareData(TYPENORMAL, ADDRESS, null);
  }

  private List<TaskFormat> getAbnormalTask() {
    return prepareTask(TASKINVALID, null, null);
  }

  private List<TaskFormat> getNormalTask() {
    return prepareTask(TASKVALID, REQUEST, REQUESTMSG);
  }

  private List<DataFormat> prepareData(String type, String address, String name) {
    List<DataFormat> result = new ArrayList<>();
    for (int i = 0; i < IOLENGTH; i++) {
      DataFormat input = new DataFormat();
      input.setDataType(type);
      input.setDataSource(address);
      input.setName(name);
      result.add(input);
    }
    return result;
  }

  private List<TaskFormat> prepareTask(String name, String key, String value) {
    List<TaskFormat> result = new ArrayList<>();
    for (int i = 0; i < TASKLENGTH; i++) {
      TaskFormat task = new TaskFormat();
      task.setName(name);
      TaskModelParam table = new TaskModelParam();
      table.put(key, value);
      task.setParams(table);
      result.add(task);
    }
    return result;
  }

}

