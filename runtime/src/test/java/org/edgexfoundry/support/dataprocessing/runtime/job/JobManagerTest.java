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
package org.edgexfoundry.support.dataprocessing.runtime.job;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.DataFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobGroupFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobInfoFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.JobGroupResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.JobResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TaskFormat;
import org.edgexfoundry.support.dataprocessing.runtime.db.JobTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.engine.Engine;
import org.edgexfoundry.support.dataprocessing.runtime.engine.EngineFactory;
import org.edgexfoundry.support.dataprocessing.runtime.engine.EngineType;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.*;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PowerMockIgnore("javax.net.ssl.*")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(PowerMockRunner.class)
@PrepareForTest({JobTableManager.class, Engine.class, EngineFactory.class})
public class JobManagerTest {
    @InjectMocks
    private static JobManager jobManager;

    @Mock
    private static JobTableManager jobTableManager;

    @Mock
    private static Engine framework;

    private static List<Map<String, String>> mockDB;
    private static JobResponseFormat response;

    @BeforeClass
    public static void setupEnvironment() throws Exception {
//        MockitoAnnotations.initMocks(this
        mockStatic(JobTableManager.class);
        mockStatic(EngineFactory.class);
        mockStatic(Engine.class);

        jobTableManager = Mockito.mock(JobTableManager.class);
        framework = Mockito.mock(Engine.class);

//        this.jobTableManager = mock(JobTableManager.class);

        when(JobTableManager.getInstance()).thenReturn(jobTableManager);
        when(EngineFactory.createEngine(EngineType.Flink)).thenReturn(framework);
        jobManager = JobManager.getInstance();
    }

    @Test
    public void aGetInstanceTest() {
        jobManager = JobManager.getInstance();
        Assert.assertNotNull(jobManager);

        JobManager jobMgrRetry = JobManager.getInstance();
        Assert.assertEquals(jobManager, jobMgrRetry);
    }

    @Test
    public void bInitializeTest() throws Exception {
        mockDB = makeMockPayload();
        response = new JobResponseFormat("ef91c5b3-b5bb-4e8b-8145-e271ac16cef1");
        when(jobTableManager.getAllJobs()).thenReturn(mockDB);
        when(framework.run(anyString())).thenReturn(response);

        jobManager.initialize();
    }

    @Test
    public void cGetJobTest() throws Exception {
        JobGroupResponseFormat jobGroupResponse;
        jobGroupResponse = jobManager.getAllJobs();
        Assert.assertFalse(jobGroupResponse.getJobGroups().size() != 2);

        jobGroupResponse = jobManager.getJob("ef91c5b3-b5bb-4e8b-8145-e271ac16ce10");
        Assert.assertFalse(jobGroupResponse.getJobGroups().size() != 1);
    }

    //FIXLATER: @Test
    public void dChangeJobStateTest() throws Exception {
        JobGroupResponseFormat jobGroupResponse;
        response = new JobResponseFormat("ef91c5b3-b5bb-4e8b-8145-e271ac16ce10");
        when(framework.run(anyString())).thenReturn(response);
        when(framework.stop(anyString())).thenReturn(response);

        jobGroupResponse = jobManager.getJob("ef91c5b3-b5bb-4e8b-8145-e271ac16ce10");
        Assert.assertEquals(jobGroupResponse.getJobGroups().get(0).getJobs().get(0).getState(), JobState.CREATE);

        jobManager.executeJob("ef91c5b3-b5bb-4e8b-8145-e271ac16ce10");
        jobGroupResponse = jobManager.getJob("ef91c5b3-b5bb-4e8b-8145-e271ac16ce10");
        Assert.assertEquals(jobGroupResponse.getJobGroups().get(0).getJobs().get(0).getState(), JobState.RUNNING);

        jobManager.stopJob("ef91c5b3-b5bb-4e8b-8145-e271ac16ce10");
        jobGroupResponse = jobManager.getJob("ef91c5b3-b5bb-4e8b-8145-e271ac16ce10");
        Assert.assertEquals(jobGroupResponse.getJobGroups().get(0).getJobs().get(0).getState(), JobState.STOPPED);

        jobManager.executeJob("ef91c5b3-b5bb-4e8b-8145-e271ac16ce10");
        jobGroupResponse = jobManager.getJob("ef91c5b3-b5bb-4e8b-8145-e271ac16ce10");
        Assert.assertEquals(jobGroupResponse.getJobGroups().get(0).getJobs().get(0).getState(), JobState.RUNNING);
    }

    //FIXLATER: @Test
    public void eCrudJobTest() throws Exception {
        JobGroupResponseFormat jobGroupResponse;

        jobGroupResponse = jobManager.getAllJobs();
        Assert.assertEquals(jobGroupResponse.getJobGroups().size(), 2);

        response = new JobResponseFormat("ef91c5b3-b5bb-4e8b-8145-e271ac16ce30");
        when(framework.createJob()).thenReturn(response);

        JobGroupFormat jobGroup = new JobGroupFormat();
        JobInfoFormat job = new JobInfoFormat();
        DataFormat input = new DataFormat("ZMQ", "127.0.0.1:5555:topic");
        DataFormat output = new DataFormat("ZMQ", "127.0.0.1:5555:topic");
        TaskFormat task = new TaskFormat(TaskType.PREPROCESSING, "CSVPARSER",
               "{\"delimiter\":\"\\t\",\"index\":\"0\"}}");
        job.addInput(input);
        job.addOutput(output);
        job.addTask(task);
        jobGroup.addJob(job);

        jobManager.createGroupJob(jobGroup);

        jobGroupResponse = jobManager.getAllJobs();
        Assert.assertEquals(jobGroupResponse.getJobGroups().size(), 3);

        jobManager.updateJob("ef91c5b3-b5bb-4e8b-8145-e271ac16ce30", jobGroup);
        jobGroupResponse = jobManager.getAllJobs();
        Assert.assertEquals(jobGroupResponse.getJobGroups().size(), 3);

        response = new JobResponseFormat("ef91c5b3-b5bb-4e8b-8145-e271ac16ce40");
        when(framework.createJob()).thenReturn(response);

        jobManager.updateJob("ef91c5b3-b5bb-4e8b-8145-e271ac16ce40", jobGroup);
        jobGroupResponse = jobManager.getAllJobs();
        Assert.assertEquals(jobGroupResponse.getJobGroups().size(), 4);

//        jobGroup.getJobs().get(0).setJobId("ef91c5b3-b5bb-4e8b-8145-e271ac16cefd");
//        jobManager.updateJob("ef91c5b3-b5bb-4e8b-8145-e271ac16cef4", jobGroup);
//
//        jobManager.updateJob("ef91c5b3-b5bb-4e8b-8145-e271ac16cef1", jobGroup);
        jobManager.deleteJob("ef91c5b3-b5bb-4e8b-8145-e271ac16ce10");
        jobGroupResponse = jobManager.getAllJobs();
        Assert.assertEquals(jobGroupResponse.getJobGroups().size(), 3);

        jobManager.deleteAllJob();
        jobGroupResponse = jobManager.getAllJobs();
        Assert.assertEquals(jobGroupResponse.getJobGroups().size(), 0);
    }

    private static List<Map<String, String>> makeMockPayload() {
        List<Map<String, String>> payload = new ArrayList<>();
        Map<String, String> p = new HashMap<>();

        // invalid param
        // Input
        JsonArray input = new JsonArray();
        JsonObject inputZMQ = new JsonObject();
        inputZMQ.addProperty("dataSource", "localhost:5555:topic");
        inputZMQ.addProperty("dataType", "ZMQ");
        input.add(inputZMQ);
        p.put(JobTableManager.Entry.input.name(), input.toString());

        // Output
        JsonArray output = new JsonArray();
        JsonObject outputZMQ = new JsonObject();
        outputZMQ.addProperty("dataSource", "localhost:5555:topic");
        outputZMQ.addProperty("dataType", "ZMQ");
        output.add(outputZMQ);
        p.put(JobTableManager.Entry.output.name(), output.toString());

        // Task
        JsonArray tasks = new JsonArray();
        JsonObject taskA = new JsonObject();
        taskA.addProperty("type", "PREPROCESSING");
        taskA.addProperty("name", "CsvParser");
        JsonObject taskAParam = new JsonObject();
        taskAParam.addProperty("delimiter", "\t");
        taskAParam.addProperty("index", "0");
        taskA.add("params", taskAParam);
        tasks.add(taskA);
        p.put(JobTableManager.Entry.taskinfo.name(), tasks.toString());
        payload.add(p);

        // valid param - create
        Map<String, String> p2 = new HashMap<>(p);
        p2.put(JobTableManager.Entry.jid.name(), "ef91c5b3-b5bb-4e8b-8145-e271ac16ce11");
        p2.put(JobTableManager.Entry.gid.name(), "ef91c5b3-b5bb-4e8b-8145-e271ac16ce10");
        p2.put(JobTableManager.Entry.state.name(), JobState.CREATE.toString());
        payload.add(p2);

        // valid param - running
        Map<String, String> p3 = new HashMap<>(p2);
        p3.put(JobTableManager.Entry.jid.name(), "ef91c5b3-b5bb-4e8b-8145-e271ac16ce21");
        p3.put(JobTableManager.Entry.gid.name(), "ef91c5b3-b5bb-4e8b-8145-e271ac16ce20");
        p3.put(JobTableManager.Entry.state.name(), JobState.RUNNING.toString());
        payload.add(p3);
        return payload;
    }
}
