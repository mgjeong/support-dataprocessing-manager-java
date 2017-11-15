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

package org.edgexfoundry.support.dataprocessing.runtime.engine.flink;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.edgexfoundry.support.dataprocessing.runtime.db.JobTableManager;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class LauncherTest {
    @Test
    public void testWithoutAnyCommandLineArguments() throws Exception {
        try {
            Launcher.main(new String[] {});
            Assert.fail("Should not reach here.");
        } catch (RuntimeException e) {
        }
    }

    @Test
    public void testWithJobId() throws Exception {
        String jobId = "test-job-id";
        Launcher launcher = new Launcher();
        try {
            launcher.execute(new String[] {"--jobId", jobId});
            Assert.fail("Should not reach here.");
        } catch (Exception e) {
            // we expect job id to be missing at this point.
        }

        // add mock
        // test with empty payload
        List<Map<String, String>> payload = new ArrayList<>();
        JobTableManager jobTableManager = mock(JobTableManager.class);
        when(jobTableManager.getPayloadById(anyString())).thenReturn(payload);
        Field fieldJobTableManager = Launcher.class.getDeclaredField("jobTableManager");
        fieldJobTableManager.setAccessible(true);
        fieldJobTableManager.set(launcher, jobTableManager);

        try {
            launcher.execute(new String[] {"--jobId", jobId});
            Assert.fail("Should not reach here.");
        } catch (Exception e) {
            // we expect no payload for the job id found at this point.
        }

        // make payload
        payload = makeMockPayload();
        jobTableManager = mock(JobTableManager.class);
        when(jobTableManager.getPayloadById(anyString())).thenReturn(payload);
        fieldJobTableManager.set(launcher, jobTableManager);

        // create spy env
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamExecutionEnvironment envSpy = spy(env);
        Field fieldEnv = Launcher.class.getDeclaredField("env");
        fieldEnv.setAccessible(true);
        fieldEnv.set(launcher, envSpy);
        doReturn(null).when(envSpy).execute(any());

        // launch
        launcher.execute(new String[] {"--jobId", jobId});
    }

    @Test
    public void testWithInvalidMockPayload() throws Exception {
        String jobId = "test-job-id";
        Launcher launcher = new Launcher();

        List<Map<String, String>> payload = makeInvalidMockPayloadInput();
        JobTableManager jobTableManager = mock(JobTableManager.class);
        when(jobTableManager.getPayloadById(anyString())).thenReturn(payload);
        Field fieldJobTableManager = Launcher.class.getDeclaredField("jobTableManager");
        fieldJobTableManager.setAccessible(true);
        fieldJobTableManager.set(launcher, jobTableManager);

        // create spy env
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamExecutionEnvironment envSpy = spy(env);
        Field fieldEnv = Launcher.class.getDeclaredField("env");
        fieldEnv.setAccessible(true);
        fieldEnv.set(launcher, envSpy);
        doReturn(null).when(envSpy).execute(any());

        // launch
        try {
            launcher.execute(new String[] {"--jobId", jobId});
            Assert.fail("Should not reach here.");
        } catch (Exception e) {
            // OK
        }

        payload = makeInvalidMockPayloadOutput();
        when(jobTableManager.getPayloadById(anyString())).thenReturn(payload);
        fieldJobTableManager.setAccessible(true);
        fieldJobTableManager.set(launcher, jobTableManager);
        // launch
        try {
            launcher.execute(new String[] {"--jobId", jobId});
            Assert.fail("Should not reach here.");
        } catch (Exception e) {
            // OK
        }
    }

    private List<Map<String, String>> makeInvalidMockPayloadOutput() {
        List<Map<String, String>> payload = new ArrayList<>();
        Map<String, String> p = new HashMap<>();

        // Input
        JsonArray input = new JsonArray();
        p.put(JobTableManager.Entry.input.name(), input.toString());

        // Output
        JsonArray output = new JsonArray();
        JsonObject outputZMQ = new JsonObject();
        outputZMQ.addProperty("dataSource", "localhost:5555:topic");
        outputZMQ.addProperty("dataType", "InvalidInput");
        output.add(outputZMQ);
        p.put(JobTableManager.Entry.output.name(), output.toString());

        // Task
        JsonArray tasks = new JsonArray();
        p.put(JobTableManager.Entry.taskinfo.name(), tasks.toString());

        payload.add(p);
        return payload;
    }

    private List<Map<String, String>> makeInvalidMockPayloadInput() {
        List<Map<String, String>> payload = new ArrayList<>();
        Map<String, String> p = new HashMap<>();

        // Input
        JsonArray input = new JsonArray();
        JsonObject inputZMQ = new JsonObject();
        inputZMQ.addProperty("dataSource", "localhost:5555:topic");
        inputZMQ.addProperty("dataType", "InvalidInput");
        input.add(inputZMQ);
        p.put(JobTableManager.Entry.input.name(), input.toString());

        // Output
        JsonArray output = new JsonArray();
        p.put(JobTableManager.Entry.output.name(), output.toString());

        // Task
        JsonArray tasks = new JsonArray();
        p.put(JobTableManager.Entry.taskinfo.name(), tasks.toString());

        payload.add(p);
        return payload;
    }

    private List<Map<String, String>> makeMockPayload() {
        List<Map<String, String>> payload = new ArrayList<>();
        Map<String, String> p = new HashMap<>();

        // Input
        JsonArray input = new JsonArray();
        JsonObject inputZMQ = new JsonObject();
        inputZMQ.addProperty("dataSource", "localhost:5555:topic");
        inputZMQ.addProperty("dataType", "ZMQ");
        input.add(inputZMQ);
        JsonObject inputEMF = new JsonObject();
        inputEMF.addProperty("dataSource", "localhost:5555:PROTOBUF_MSG");
        inputEMF.addProperty("dataType", "EMF");
        input.add(inputEMF);
        p.put(JobTableManager.Entry.input.name(), input.toString());

        // Output
        JsonArray output = new JsonArray();
        JsonObject outputZMQ = new JsonObject();
        outputZMQ.addProperty("dataSource", "localhost:5555:topic");
        outputZMQ.addProperty("dataType", "ZMQ");
        output.add(outputZMQ);
        JsonObject outputEMF = new JsonObject();
        outputEMF.addProperty("dataSource", "localhost:5555:PROTOBUF_MSG");
        outputEMF.addProperty("dataType", "EMF");
        output.add(outputEMF);
        JsonObject outputPayload = new JsonObject();
        outputPayload.addProperty("dataSource", "output");
        outputPayload.addProperty("dataType", "F");
        output.add(outputPayload);
        JsonObject outputWebSocket = new JsonObject();
        outputWebSocket.addProperty("dataSource", "localhost:5555");
        outputWebSocket.addProperty("dataType", "WS");
        output.add(outputWebSocket);
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
        return payload;
    }
}
