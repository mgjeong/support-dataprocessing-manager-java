/*******************************************************************************
 * Copyright 2018 Samsung Electronics All Rights Reserved.
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;

import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState.State;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.Workflow;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData.EngineType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.db.JobTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.db.WorkflowTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.engine.Engine;
import org.edgexfoundry.support.dataprocessing.runtime.engine.EngineFactory;
import org.edgexfoundry.support.dataprocessing.runtime.engine.EngineManager;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.FlinkEngine;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {WorkflowTableManager.class, JobTableManager.class,
    EngineManager.class, EngineFactory.class})
public class JobControllerTest {

  private static final Gson gson = new Gson();

  private WorkflowTableManager workflowTableManager = mock(WorkflowTableManager.class);
  private JobTableManager jobTableManager = mock(JobTableManager.class);
  private EngineManager engineManager = mock(EngineManager.class);

  @Test
  public void testValidateWorkflow() throws Exception {
    JobController jobController = createJobController();
    Workflow sampleWorkflow = createSampleWorkflow();
    doReturn(sampleWorkflow).when(workflowTableManager).getWorkflow(sampleWorkflow.getId());
    ResponseEntity response = jobController.validateWorkflow(sampleWorkflow.getId());

    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testDeployWorkflow() throws Exception {
    JobController jobController = spy(createJobController());
    Workflow workflow = createSampleWorkflow();
    WorkflowData workflowData = createSampleWorkflowData(workflow);
    Job job = createSampleJob(workflowData);
    Engine engine = mock(FlinkEngine.class);

    doReturn(engine).when(engineManager).getEngine(anyString(), anyInt(), any());
    doReturn(workflow).when(workflowTableManager).getWorkflow(workflow.getId());
    doReturn(workflowData).when(workflowTableManager).doExportWorkflow(workflow);
    doReturn(job).when(jobTableManager).addJob(any());
    doReturn(job.getState()).when(jobTableManager).updateJobState(any());

    ResponseEntity response = jobController.deployWorkflow(workflow.getId());
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assert.assertTrue(isValidJson(response.getBody().toString()));

    // Test exceptions
    doThrow(new RuntimeException("mocked")).when(engine).run(any());
    response = jobController.deployWorkflow(workflow.getId());
    Assert.assertTrue(isValidJson(response.getBody().toString()));

    doThrow(new RuntimeException("mocked")).when(workflowTableManager).getWorkflow(any());
    response = jobController.deployWorkflow(workflow.getId());
    Assert.assertTrue(isValidJson(response.getBody().toString()));
  }

  @Test
  public void testStopJob() throws Exception {
    JobController jobController = spy(createJobController());
    Workflow workflow = createSampleWorkflow();
    WorkflowData workflowData = createSampleWorkflowData(workflow);
    Job job = createSampleJob(workflowData);
    Engine engine = mock(FlinkEngine.class);

    doReturn(engine).when(engineManager).getEngine(anyString(), anyInt(), any());
    doReturn(job).when(jobTableManager).getJobById(any());
    doReturn(job.getState()).when(jobTableManager).updateJobState(any());

    ResponseEntity response = jobController.stopJob(workflow.getId(), job.getId());
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assert.assertTrue(isValidJson(response.getBody().toString()));

    // test exception
    doThrow(new RuntimeException("mocked")).when(engine).stop(any());
    response = jobController.stopJob(workflow.getId(), job.getId());
    Assert.assertTrue(isValidJson(response.getBody().toString()));

    doThrow(new RuntimeException("mocked")).when(jobTableManager).getJobById(any());
    response = jobController.stopJob(workflow.getId(), job.getId());
    Assert.assertTrue(isValidJson(response.getBody().toString()));
  }

  @Test
  public void testMonitorJobs() throws Exception {
    JobController jobController = spy(createJobController());
    Collection<Job> jobs = new ArrayList<>();
    Workflow workflowA = createSampleWorkflow();
    workflowA.setId(1L);
    Job jobA = createSampleJob(createSampleWorkflowData(workflowA));
    jobA.getState().setState(State.RUNNING);
    jobs.add(jobA);
    Job jobB = createSampleJob(createSampleWorkflowData(workflowA));
    jobB.getState().setState(State.STOPPED);
    jobs.add(jobB);
    doReturn(jobs).when(jobTableManager).getJobs();

    ResponseEntity response = jobController.monitorJobs();
    Assert.assertTrue(isValidJson(response.getBody().toString()));

    doThrow(new RuntimeException("Mocked")).when(jobTableManager).getJobs();
    response = jobController.monitorJobs();
    Assert.assertTrue(isValidJson(response.getBody().toString()));
  }

  @Test
  public void testMonitorJob() throws Exception {
    JobController jobController = spy(createJobController());
    Collection<Job> jobs = new ArrayList<>();
    Workflow workflowA = createSampleWorkflow();
    workflowA.setId(1L);
    Job jobA = createSampleJob(createSampleWorkflowData(workflowA));
    jobA.getState().setState(State.RUNNING);
    jobs.add(jobA);
    Job jobB = createSampleJob(createSampleWorkflowData(workflowA));
    jobB.getState().setState(State.STOPPED);
    jobs.add(jobB);
    doReturn(jobs).when(jobTableManager).getJobsByWorkflow(any());

    ResponseEntity response = jobController.monitorJob(workflowA.getId());
    Assert.assertTrue(isValidJson(response.getBody().toString()));

    doThrow(new RuntimeException("Mocked")).when(jobTableManager).getJobsByWorkflow(any());
    response = jobController.monitorJob(workflowA.getId());
    Assert.assertTrue(isValidJson(response.getBody().toString()));
  }

  @Test
  public void testMonitorJobDetails() throws Exception {
    JobController jobController = spy(createJobController());
    Collection<Job> jobs = new ArrayList<>();
    Workflow workflowA = createSampleWorkflow();
    workflowA.setId(1L);
    Job jobA = createSampleJob(createSampleWorkflowData(workflowA));
    jobA.getState().setState(State.RUNNING);
    jobs.add(jobA);
    Job jobB = createSampleJob(createSampleWorkflowData(workflowA));
    jobB.getState().setState(State.STOPPED);
    jobs.add(jobB);
    doReturn(jobs).when(jobTableManager).getJobsByWorkflow(any());

    ResponseEntity response = jobController.monitorJobDetails(workflowA.getId());
    Assert.assertTrue(isValidJson(response.getBody().toString()));

    doThrow(new RuntimeException("Mocked")).when(jobTableManager).getJobsByWorkflow(any());
    response = jobController.monitorJobDetails(workflowA.getId());
    Assert.assertTrue(isValidJson(response.getBody().toString()));
  }

  private boolean isValidJson(String s) {
    if (StringUtils.isEmpty(s)) {
      return false;
    }

    try {
      gson.fromJson(s, Object.class);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private Job createSampleJob(WorkflowData workflowData) {
    Job job = Job.create(workflowData);
    job.getState().setState(State.RUNNING);
    job.getState().setStartTime(System.currentTimeMillis());
    return job;
  }

  private WorkflowData createSampleWorkflowData(Workflow workflow) {

    WorkflowData data = new WorkflowData();
    data.setWorkflowId(workflow.getId());
    data.getConfig().put("targetHost", "localhost:5555");

    WorkflowProcessor workflowProcessor = new WorkflowProcessor();
    workflowProcessor.setEngineType(EngineType.FLINK.name());

    List<WorkflowProcessor> list = new ArrayList<>();
    list.add(workflowProcessor);

    data.setProcessors(list);

    return data;
  }

  private Workflow createSampleWorkflow() {
    Workflow workflow = new Workflow();
    workflow.setId(1L);
    workflow.setName("sample workflow");

    return workflow;
  }

  private JobController createJobController() throws Exception {
    JobController jobController = new JobController();
    Field wtm = jobController.getClass().getDeclaredField("workflowTableManager");
    wtm.setAccessible(true);
    wtm.set(jobController, workflowTableManager);
    Field jtm = jobController.getClass().getDeclaredField("jobTableManager");
    jtm.setAccessible(true);
    jtm.set(jobController, jobTableManager);
    Field em = jobController.getClass().getDeclaredField("engineManager");
    em.setAccessible(true);
    em.set(jobController, engineManager);
    return jobController;
  }
}
