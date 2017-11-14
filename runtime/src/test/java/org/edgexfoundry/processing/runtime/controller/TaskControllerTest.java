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
package org.edgexfoundry.processing.runtime.controller;

import org.edgexfoundry.processing.runtime.data.model.Format;
import org.edgexfoundry.processing.runtime.data.model.error.ErrorFormat;
import org.edgexfoundry.processing.runtime.data.model.response.JobResponseFormat;
import org.edgexfoundry.processing.runtime.data.model.response.ResponseFormat;
import org.edgexfoundry.processing.runtime.data.model.response.TaskResponseFormat;
import org.edgexfoundry.processing.runtime.data.model.task.TaskFormat;
import org.edgexfoundry.processing.runtime.task.TaskManager;
import org.edgexfoundry.processing.runtime.task.TaskType;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@PrepareForTest(TaskManager.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@RunWith(PowerMockRunner.class)
public class TaskControllerTest {

    private static MockMvc mockMvc;

    @InjectMocks
    private static TaskController taskController;

    @Mock
    private static TaskManager taskManager;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeClass
    public static void setUp() {
        mockStatic(TaskManager.class);
        taskManager = mock(TaskManager.class);
        when(TaskManager.getInstance()).thenReturn(taskManager);
        taskManager = TaskManager.getInstance();
    }

    @Before
    public void setUpMvc() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
    }

    @Test
    public void getTasksTest() throws Exception {
        ResultActions result;
        String content;
        TaskResponseFormat response;
        List<TaskFormat> mockResponse = makeMockResponse();

        when(taskManager.getTaskModelList()).thenReturn(mockResponse);

        result = mockMvc.perform(get("/v1/task").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        content = result.andReturn().getResponse().getContentAsString();
        Assert.assertNotNull(content);
        response = Format.create(content, TaskResponseFormat.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getTask().size(), 1);

        when(taskManager.getTaskModel((TaskType) notNull())).thenReturn(mockResponse);

        result = mockMvc.perform(get("/v1/task/type/REGRESSION")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        content = result.andReturn().getResponse().getContentAsString();
        Assert.assertNotNull(content);
        response = Format.create(content, TaskResponseFormat.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getTask().size(), 1);

        when(taskManager.getTaskModel(anyString())).thenReturn(mockResponse);

        result = mockMvc.perform(get("/v1/task/name/LinearRegression")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        content = result.andReturn().getResponse().getContentAsString();
        Assert.assertNotNull(content);
        response = Format.create(content, TaskResponseFormat.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getTask().size(), 1);
    }

    @Test
    public void addTaskTest() throws Exception {
        FileInputStream fis = getFileInputStreamFromFile("test-model.jar");

        MockMultipartFile multipartFile = new MockMultipartFile("file",
                "test-model.jar",
                "text/plain", fis);

        when(taskManager.addTask(anyString(), (byte[]) notNull())).thenReturn(new ErrorFormat());

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.fileUpload("/v1/task")
                .file(multipartFile)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String content = result.andReturn().getResponse().getContentAsString();
        Assert.assertNotNull(content);
        JobResponseFormat response = Format.create(content, JobResponseFormat.class);
        Assert.assertNotNull(response);
    }

    @Test
    public void deleteTaskTest() throws Exception {
        when(taskManager.deleteTask((TaskType) notNull(), anyString())).thenReturn(new ErrorFormat());

        ResultActions result = mockMvc.perform(post("/v1/task/delete")
                .param("type", "TREND")
                .param("name", "sma")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String content = result.andReturn().getResponse().getContentAsString();
        Assert.assertNotNull(content);
        ResponseFormat response = Format.create(content, ResponseFormat.class);
        Assert.assertNotNull(response);

    }

    private List<TaskFormat> makeMockResponse() {
        List<TaskFormat> response = new ArrayList<>();
        TaskFormat task = null;
        task = new TaskFormat(TaskType.REGRESSION, "LinearRegression", "{\"weights\":\"0.228758\"}");
        response.add(task);
        return response;
    }

    private FileInputStream getFileInputStreamFromFile(String path) throws Exception {
        URL resource = this.getClass().getClassLoader().getResource(path);
        File file = new File(resource.toURI());
        return new FileInputStream(file);
    }
}
