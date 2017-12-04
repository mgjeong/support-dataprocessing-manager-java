/*******************************************************************************
 * Copyright 2017 Samsung Electronics All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.edgexfoundry.support.dataprocessing.runtime.controller;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.DataFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobGroupFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobInfoFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.JobGroupResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.JobResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TaskFormat;
import org.edgexfoundry.support.dataprocessing.runtime.engine.EngineType;
import org.edgexfoundry.support.dataprocessing.runtime.job.JobManager;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@PrepareForTest(JobManager.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@RunWith(PowerMockRunner.class)
public class JobControllerTest {

    private static MockMvc mockMvc;

    @InjectMocks
    private JobController jobController;

    @Mock
    private static JobManager jobManager;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private static final String JSON_JOB_CREATE = "{\"jobs\":[{\"input\":[{\"dataType\":\"ZMQ\","
            + "\"dataSource\":\"10.113.64.225:5555:topic\"}],"
            + "\"output\":[{\"dataType\":\"ZMQ\",\"dataSource\":\"127.0.0.1:5556:topic\"}],"
            + "\"task\":[{\"type\":\"TREND\",\"name\":\"sma\",\"params\""
            + ":{\"interval\":{\"data\":1}}},{\"type\":\"PREPROCESSING\",\"name\":"
            + "\"JsonGenerator\",\"params\":{\"keys\":\"1\"}}],\"state\":\"CREATE\"},"
            + "{\"input\":[{\"dataType\":\"ZMQ\",\"dataSource\":\"10.113.64.225:5555:topic\"}],"
            + "\"output\":[{\"dataType\":\"ZMQ\",\"dataSource\":\"127.0.0.1:5557:topic\"}],"
            + "\"task\":[{\"type\":\"TREND\",\"name\":\"sma\","
            + "\"params\":{\"interval\":{\"data\":10}}},"
            + "{\"type\":\"PREPROCESSING\",\"name\":\"JsonGenerator\",\"params\":{\"keys\":\"10\"}}],"
            + "\"state\":\"CREATE\"},{\"input\":[{\"dataType\":\"ZMQ\",\"dataSource\":\"10.113.64.225:5555:topic\"}],"
            + "\"output\":[{\"dataType\":\"ZMQ\",\"dataSource\":\"127.0.0.1:5558:topic\"}],"
            + "\"task\":[{\"type\":\"TREND\",\"name\":\"sma\",\"params\":{\"interval\":{\"data\":20}}},"
            + "{\"type\":\"PREPROCESSING\",\"name\":\"JsonGenerator\",\"params\":{\"keys\":\"20\"}}],"
            + "\"state\":\"CREATE\"},{\"input\":[{\"dataType\":\"ZMQ\",\"dataSource\":\"127.0.0.1:5556:topic\"},"
            + "{\"dataType\":\"ZMQ\",\"dataSource\":\"127.0.0.1:5557:topic\"},"
            + "{\"dataType\":\"ZMQ\",\"dataSource\":\"127.0.0.1:5558:topic\"}],"
            + "\"output\":[{\"dataType\":\"WS\",\"dataSource\":\"10.113.64.225:8083\"}],"
            + "\"task\":[{\"type\":\"PREPROCESSING\",\"name\":\"Aggregator\","
            + "\"params\":{\"keys\":\"1 10 20\",\"aggregateBy\":\"Id\"}},"
            + "{\"type\":\"PREPROCESSING\",\"name\":\"JsonGenerator\","
            + "\"params\":{\"keys\":\"1 10 20\"}}],\"state\":\"CREATE\"}]}";

    @BeforeClass
    public static void setUp() {
        mockStatic(JobManager.class);
        jobManager = mock(JobManager.class);
        when(JobManager.getInstance()).thenReturn(jobManager);
        jobManager = JobManager.getInstance();
    }

    @Before
    public void setUpMvc() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(jobController).build();
    }

    @Test
    public void getJobTest() throws Exception {
        JobGroupResponseFormat mockResponse = makeMockResponse();

        when(jobManager.getAllJobs()).thenReturn(mockResponse);

        ResultActions result = mockMvc.perform(get("/v1/job")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String content = result.andReturn().getResponse().getContentAsString();
        Assert.assertNotNull(content);
        JobGroupResponseFormat response = Format.create(content, JobGroupResponseFormat.class);
        Assert.assertNotNull(response);

        when(jobManager.getJob(anyString())).thenReturn(mockResponse);
        result = mockMvc.perform(get("/v1/job/ef91c5b3-b5bb-4e8b-8145-e271ac16ce30")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        content = result.andReturn().getResponse().getContentAsString();
        Assert.assertNotNull(content);
        response = Format.create(content, JobGroupResponseFormat.class);
        Assert.assertNotNull(response);
    }

    @Test
    public void createJobTest() throws Exception {
        JobResponseFormat mockResponse = makeMockJobResponse();

        when(jobManager.createGroupJob(any(EngineType.class), (JobGroupFormat) notNull())).thenReturn(mockResponse);

        ResultActions result = mockMvc.perform(post("/v1/job")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_JOB_CREATE)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String content = result.andReturn().getResponse().getContentAsString();
        Assert.assertNotNull(content);
        JobResponseFormat response = Format.create(content, JobResponseFormat.class);
        Assert.assertNotNull(response);
    }

    @Test
    public void deleteJobTest() throws Exception {
        JobResponseFormat mockResponse = makeMockJobResponse();
        ErrorFormat mockError = makeMockErrorFormat();

        when(jobManager.deleteJob(anyString())).thenReturn(mockResponse);

        ResultActions result = mockMvc.perform(delete("/v1/job/ef91c5b3-b5bb-4e8b-8145-e271ac16ce30")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String content = result.andReturn().getResponse().getContentAsString();
        Assert.assertNotNull(content);
        JobResponseFormat response = Format.create(content, JobResponseFormat.class);
        Assert.assertNotNull(response);

        when(jobManager.deleteAllJob()).thenReturn(mockError);

        result = mockMvc.perform(delete("/v1/job")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        content = result.andReturn().getResponse().getContentAsString();
        Assert.assertNotNull(content);
        response = Format.create(content, JobResponseFormat.class);
        Assert.assertNotNull(response);
    }

    @Test
    public void runJobTest() throws Exception {
        JobResponseFormat mockResponse = makeMockJobResponse();

        when(jobManager.executeJob(anyString())).thenReturn(mockResponse);

        ResultActions result = mockMvc.perform(post("/v1/job/ef91c5b3-b5bb-4e8b-8145-e271ac16ce30/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String content = result.andReturn().getResponse().getContentAsString();
        Assert.assertNotNull(content);
        JobResponseFormat response = Format.create(content, JobResponseFormat.class);
        Assert.assertNotNull(response);
    }

    @Test
    public void stopJobTest() throws Exception {
        JobResponseFormat mockResponse = makeMockJobResponse();


        when(jobManager.stopJob(anyString())).thenReturn(mockResponse);

        ResultActions result = mockMvc.perform(post("/v1/job/ef91c5b3-b5bb-4e8b-8145-e271ac16ce30/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String content = result.andReturn().getResponse().getContentAsString();
        Assert.assertNotNull(content);
        JobResponseFormat response = Format.create(content, JobResponseFormat.class);
        Assert.assertNotNull(response);
    }

    @Test
    public void updateJobsTest() throws Exception {
        JobResponseFormat mockResponse = makeMockJobResponse();

        when(jobManager.updateJob(any(EngineType.class), anyString(), (JobGroupFormat) notNull())).thenReturn(mockResponse);

        String jsonUpdate = "{\"jobs\":[{\"targetHost\":\"localhost:8082\",\"input\":[{\"dataType\":\"ZMQ\","
                + "\"dataSource\":\"localhost:5555:topic\"}],"
                + "\"output\":[{\"dataType\":\"F\",\"dataSource\":\"output\"}],"
                + "\"task\":[{\"type\":\"REGRESSION\",\"name\":\"LogisticRegression\","
                + "\"params\":{\"error\":\"-1.704e16\","
                + "\"weights\":\"-4.508e15 -2.681e13 5.592e14 6.374e12 4.804e16 -2.931e16 3.110e14 "
                + "8.171e15 -8.564e15 -1.030e16 8.161e15 -4.617e14 -8.989e14 6.591e12 -1.037e16 4.729e16 "
                + "-2.689e16 1.993e17 -4.806e16 -4.364e17 1.102e15 1.390e14 6.421e13 -9.950e12 -6.379e15 "
                + "-7.493e15 6.282e15 -7.308e15 1.098e16 4.755e16\"}}],\"state\":\"CREATE\"}]}";

        ResultActions result = mockMvc.perform(put("/v1/job/ef91c5b3-b5bb-4e8b-8145-e271ac16ce30")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonUpdate)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String content = result.andReturn().getResponse().getContentAsString();
        Assert.assertNotNull(content);
        JobResponseFormat groupResponse = Format.create(content, JobResponseFormat.class);
        Assert.assertNotNull(groupResponse);
    }

    private ErrorFormat makeMockErrorFormat() {
        ErrorFormat payload = new ErrorFormat();
        return payload;
    }

    private JobResponseFormat makeMockJobResponse() {
        JobResponseFormat payload = new JobResponseFormat();
        payload.setJobId("ef91c5b3-b5bb-4e8b-8145-e271ac16ce30");
//        payload.setError();
        return payload;
    }

    private JobGroupResponseFormat makeMockResponse() {
        JobGroupResponseFormat payload = new JobGroupResponseFormat();
        JobGroupFormat jobs = new JobGroupFormat();
        jobs.setGroupId("ef91c5b3-b5bb-4e8b-8145-e271ac16ce30");

        JobInfoFormat job = new JobInfoFormat();
        job.setJobId("ef91c5b3-b5bb-4e8b-8145-e271ac16ce31");

        DataFormat input = new DataFormat("ZMQ", "127.0.0.1:5555:topic");
        DataFormat output = new DataFormat("ZMQ", "127.0.0.1:5555:topic");
        TaskFormat task = new TaskFormat(TaskType.PREPROCESSING, "CSVPARSER",
               "{\"delimiter\":\"\\t\",\"index\":\"0\"}}");

        job.addInput(input);
        job.addOutput(output);
        job.addTask(task);
        jobs.addJob(job);
        payload.addJobGroup(jobs);

        return payload;
    }
}
