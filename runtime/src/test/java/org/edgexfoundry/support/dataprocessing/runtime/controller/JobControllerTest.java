package org.edgexfoundry.support.dataprocessing.runtime.controller;

import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.lang.reflect.Field;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.Workflow;
import org.edgexfoundry.support.dataprocessing.runtime.db.JobTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.db.WorkflowTableManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {WorkflowTableManager.class, JobTableManager.class})
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
