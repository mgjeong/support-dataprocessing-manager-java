package org.edgexfoundry.support.dataprocessing.runtime.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState.State;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.Workflow;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.db.JobTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.db.WorkflowTableManager;
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

  private WorkflowTableManager workflowTableManager = mock(WorkflowTableManager.class);
  private JobTableManager jobTableManager = mock(JobTableManager.class);

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

    mockStatic(EngineManager.class);

    JobController jobController = spy(createJobController());
    Workflow workflow = createSampleWorkflow();
    WorkflowData workflowData = createSampleWorkflowData(workflow);
    Job job = createSampleJob();
    FlinkEngine engine = mock(FlinkEngine.class);

    doReturn(engine).when(jobController).createEngine(any(), any());
    doReturn(job).when(engine).create(any());
    doReturn(job).when(engine).run(any());
    doReturn(workflow).when(workflowTableManager).getWorkflow(workflow.getId());
    doReturn(workflowData).when(workflowTableManager).doExportWorkflow(workflow);
    doReturn(job).when(jobTableManager).addOrUpdateWorkflowJob(any());
    doReturn(job.getState()).when(jobTableManager).addOrUpdateWorkflowJobState(any(), any());
    when(EngineManager.getEngine(anyString(), any())).thenReturn(engine);

    ResponseEntity response = jobController.deployWorkflow(workflow.getId());
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testStopJob() throws Exception {
    JobController jobController = spy(createJobController());
    Workflow workflow = createSampleWorkflow();
    Job job = createSampleJob();
    FlinkEngine engine = mock(FlinkEngine.class);

    doReturn(engine).when(jobController).createEngine(any(), any());
    doReturn(job).when(engine).stop(any());
    doReturn(job).when(jobTableManager).getWorkflowJob(any());
    doReturn(job.getState()).when(jobTableManager).addOrUpdateWorkflowJobState(any(), any());

    ResponseEntity response = jobController.stopJob(workflow.getId(), job.getId());
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testGetWorkflowJobs() throws Exception {
    JobController jobController = createJobController();
    Job job = createSampleJob();
    List<Job> jobs = new ArrayList<>();
    jobs.add(job);
    doReturn(jobs).when(jobTableManager).listWorkflowJobs(1L);
    // valid
    ResponseEntity response = jobController.getWorkflowJobs(1L);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

    doReturn(null).when(jobTableManager).listWorkflowJobs(1L);
    response = jobController.getWorkflowJobs(1L);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  private Job createSampleJob() {
    Job job = new Job();
    job.setId("1");
    job.setWorkflowId(1L);
    job.setState(new JobState());
    job.getState().setState(State.RUNNING);
    job.getState().setStartTime(System.currentTimeMillis());
    return job;
  }

  private WorkflowData createSampleWorkflowData(Workflow workflow) {

    WorkflowData data = new WorkflowData();
    data.setWorkflowId(workflow.getId());
    data.getConfig().put("targetHost", "localhost:5555");

    WorkflowProcessor workflowProcessor = new WorkflowProcessor();
    workflowProcessor.setEngineType("flink");

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
    return jobController;
  }
}
