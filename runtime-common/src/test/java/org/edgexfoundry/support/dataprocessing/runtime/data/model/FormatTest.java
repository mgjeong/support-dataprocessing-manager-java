package org.edgexfoundry.support.dataprocessing.runtime.data.model;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.DataFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobGroupFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobInfoFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.JobGroupResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.JobResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.ResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.TaskResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TaskFormat;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.junit.Assert;
import org.junit.Test;

public class FormatTest {
    @Test
    public void testJobGroupResponseFormat() {
        JobGroupResponseFormat jobGroupResponse = new JobGroupResponseFormat();
        JobGroupFormat jobGroup = new JobGroupFormat();
        jobGroup.setGroupId("af91c5b3-b5bb-4e8b-8145-e271ac16cefe");
        Assert.assertFalse(jobGroup.getJobs().size() != 0);
        Assert.assertFalse(jobGroupResponse.getJobGroups().size() != 0);

        JobInfoFormat job = new JobInfoFormat();
        DataFormat input = new DataFormat("ZMQ", "127.0.0.1:5555:topic");
        DataFormat output = new DataFormat("ZMQ", "127.0.0.1:5555:topic");
        TaskFormat task = new TaskFormat(TaskType.PREPROCESSING, "CSVPARSER",
                "{\"delimiter\":\"\\t\",\"index\":\"0\"}}");

        job.addInput(input);
        job.addOutput(output);
        job.addTask(task);
        job.setJobId("ef91c5b3-b5bb-4e8b-8145-e271ac16cefc");
        jobGroup.addJob(job);
        Assert.assertFalse(jobGroup.getJobs().size() != 1);

        JobInfoFormat job2 = new JobInfoFormat(job.getInput(), job.getOutput(), job.getTask());
        job2.setJobId("ef91c5b3-b5bb-4e8b-8145-e271ac16cefd");
        jobGroup.addJob(job2);
        Assert.assertFalse(jobGroup.getJobs().size() != 2);

        JobInfoFormat job3 = new JobInfoFormat();
        job3.setPayload(job);
        job3.setJobId("ef91c5b3-b5bb-4e8b-8145-e271ac16cefe");
        jobGroup.addJob(job3);
        Assert.assertFalse(jobGroup.getJobs().size() != 3);

        jobGroupResponse.addJobGroup(jobGroup);
        Assert.assertFalse(jobGroupResponse.getJobGroups().size() != 1);

        jobGroupResponse.addJobGroup((JobGroupFormat) jobGroup.clone());
        Assert.assertFalse(jobGroupResponse.getJobGroups().size() != 2);

        jobGroupResponse.addJobGroup(JobGroupFormat.create(jobGroup.toString(), JobGroupFormat.class));
        Assert.assertFalse(jobGroupResponse.getJobGroups().size() != 3);

        jobGroupResponse.addJobGroup(JobGroupFormat.create("error test", JobGroupFormat.class));
        Assert.assertFalse(jobGroupResponse.getJobGroups().size() != 3);
    }

    @Test
    public void testTaskResponseFormat() {
        TaskResponseFormat response = new TaskResponseFormat();

        Assert.assertNotNull(response.getTask());
        Assert.assertFalse(response.getTask().size() != 0);

        TaskFormat task = new TaskFormat(TaskType.PREPROCESSING, "CSVPARSER",
                "{\"delimiter\":\"\\t\",\"index\":\"0\"}}");
        response.addAlgorithmFormat(task);
        Assert.assertFalse(response.getTask().size() != 1);

        response.addAlgorithmFormat(new TaskFormat(task));
        Assert.assertFalse(response.getTask().size() != 2);
    }

    @Test
    public void testResponseFormat() {
        ResponseFormat response = new ResponseFormat();
        Assert.assertFalse(response.getError().isError());
    }

    @Test
    public void testJobResponseFormat() {
        JobResponseFormat response = new JobResponseFormat();
        Assert.assertTrue(response.getError().isNoError());
        Assert.assertFalse(response.getJobId() != null);

        response.setJobId("ef91c5b3-b5bb-4e8b-8145-e271ac16cefc");
        Assert.assertFalse(response.getJobId() != "ef91c5b3-b5bb-4e8b-8145-e271ac16cefc");
    }

    @Test
    public void testErrorType() {
        ErrorType error = ErrorType.DPFW_ERROR_NONE;
        Assert.assertFalse(error != ErrorType.DPFW_ERROR_NONE);
        Assert.assertFalse(error.toString().compareTo("DPFW_ERROR_NONE") != 0);

        ErrorType newError = ErrorType.getErrorType(error.toString());
        Assert.assertFalse(newError.toString().compareTo("DPFW_ERROR_NONE") != 0);
    }

    @Test
    public void testJobState() {
        JobState state = JobState.CREATE;
        Assert.assertFalse(state.toString() != "CREATE");

        JobState newState = JobState.getState(state.toString());
        Assert.assertFalse(newState.toString() != "CREATE");
    }

     @Test
    public void testTaskFormat() {
        String json = "{\"type\":\"REGRESSION\",\"name\":\"LinearRegression\","
                + "\"params\":{\"error\":\"1.235593\",\"weights\":\"0.228758 -0.156367\"},"
                + "\"inrecord\":[\"/records/x\"],\"outrecord\":[\"/records/score\"]}";
        TaskFormat taskFormat = TaskFormat.create(json, TaskFormat.class);
        Assert.assertNotNull(taskFormat);
        Assert.assertTrue(taskFormat.getInrecord().size() == 1);
        Assert.assertTrue(taskFormat.getOutrecord().size() == 1);

        System.out.println(taskFormat.toString());
    }
}
