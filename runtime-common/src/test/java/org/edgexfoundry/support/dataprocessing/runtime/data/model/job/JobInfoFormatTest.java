package org.edgexfoundry.support.dataprocessing.runtime.data.model.job;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TaskFormat;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class JobInfoFormatTest {

    @Test
    public void createInstanceTest() {

        JobInfoFormat format1 = new JobInfoFormat();
        Assert.assertNotNull(format1);

        JobInfoFormat format2 = new JobInfoFormat(new ArrayList<DataFormat>(),
                new ArrayList<DataFormat>(),
                new ArrayList<TaskFormat>());
        Assert.assertNotNull(format2);

        JobInfoFormat format3 = new JobInfoFormat(new ArrayList<DataFormat>(),
                new ArrayList<DataFormat>(),
                new ArrayList<TaskFormat>(),
                JobState.CREATE);
        Assert.assertNotNull(format3);

        JobInfoFormat format4 = new JobInfoFormat(new ArrayList<DataFormat>(),
                new ArrayList<DataFormat>(),
                new ArrayList<TaskFormat>(),
                JobState.CREATE,
                "",
                "");
        Assert.assertNotNull(format4);

    }

    @Test
    public void addTaskTest() {

        JobInfoFormat format = new JobInfoFormat();

        format.addTask(new TaskFormat());
        Assert.assertTrue(format.getTask().size() > 0);

    }

    @Test
    public void addInputTest() {

        JobInfoFormat format = new JobInfoFormat();

        format.addInput(new DataFormat());
        Assert.assertTrue(format.getInput().size() > 0);

    }

    @Test
    public void addOutputTest() {

        JobInfoFormat format = new JobInfoFormat();

        format.addOutput(new DataFormat());
        Assert.assertTrue(format.getOutput().size() > 0);

    }


    @Test
    public void getStateTest() {

        JobInfoFormat format = new JobInfoFormat();

        format.setState(JobState.RUNNING);
        Assert.assertEquals(JobState.RUNNING, format.getState());

    }

    @Test
    public void getJobId() {

        String testId = "TESETID";

        JobInfoFormat format = new JobInfoFormat();
        format.setJobId(testId);

        Assert.assertEquals(testId, format.getJobId());
    }

    @Test
    public void getTargetHost() {
        String hostInfo = "localhost:8080";

        JobInfoFormat format = new JobInfoFormat();
        format.setTargetHost(hostInfo);

        Assert.assertEquals(hostInfo, format.getTargetHost());
    }

    @Test
    public void getEngineTypeTest() {

        String engineType = "Flink";

        JobInfoFormat format = new JobInfoFormat();
        format.setEngineType(engineType);

        Assert.assertEquals(engineType, format.getEngineType());

    }

    @Test
    public void getRuntimeHost() {
        String hostInfo = "localhost:8080";

        JobInfoFormat format = new JobInfoFormat();
        format.setRuntimeHost(hostInfo);

        Assert.assertEquals(hostInfo, format.getRuntimeHost());
    }

    @Test
    public void setPayload() {

        JobInfoFormat newJobInfo = new JobInfoFormat();

        newJobInfo.addTask(new TaskFormat());
        newJobInfo.addInput(new DataFormat());
        newJobInfo.addOutput(new DataFormat());

        JobInfoFormat cloneJobInfo = new JobInfoFormat();
        cloneJobInfo.setPayload(newJobInfo);

        Assert.assertTrue(cloneJobInfo.getInput().size() > 0);
        Assert.assertTrue(cloneJobInfo.getOutput().size() > 0);
        Assert.assertTrue(cloneJobInfo.getTask().size() > 0);
    }

}
