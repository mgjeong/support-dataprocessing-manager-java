package org.edgexfoundry.support.dataprocessing.runtime.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;

import java.lang.reflect.Field;
import org.edgexfoundry.support.dataprocessing.runtime.controller.WorkflowController.ImportType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.Workflow;
import org.edgexfoundry.support.dataprocessing.runtime.db.JobTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.db.WorkflowTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.engine.EngineManager;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskManager;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.springframework.http.ResponseEntity;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {WorkflowTableManager.class, JobTableManager.class, EngineManager.class,
    TaskManager.class})
public class DeveloperControllerTest {

  private static DeveloperController developerController;

  private static WorkflowTableManager workflowTableManager = mock(WorkflowTableManager.class);
  private static JobTableManager jobTableManager = mock(JobTableManager.class);
  private static EngineManager engineManager = mock(EngineManager.class);

  @BeforeClass
  public static void setup() throws Exception {
    developerController = spy(new DeveloperController());
    WorkflowController workflowController = createWorkflowController();

    WhiteboxImpl.setInternalState(developerController, "workflowController", workflowController);
  }

  @Test
  public void testCreateWorkflow() {
    ResponseEntity entity = developerController.createWorkflow(ImportType.FILE, null, null, null);
    Assert.assertNotNull(entity);
  }

  @Test
  public void testRunWorkflow() {
    ResponseEntity entity = developerController.runWorkflow(1L);
    Assert.assertNotNull(entity);
  }

  @Test
  public void testStopJob() {
    ResponseEntity entity = developerController.stopJob(1L, "a");
    Assert.assertNotNull(entity);
  }

  @Test
  public void testRemoveWorkflow() {
    ResponseEntity entity = developerController.removeWorkflow(1L);
    Assert.assertNotNull(entity);
  }

  @Test
  public void testListWorkflows() {
    ResponseEntity entity = developerController.listWorkflows();
    Assert.assertNotNull(entity);
  }

  @Test
  public void testMonitorWorkflow() {
    ResponseEntity entity = developerController.monitorWorkflow(1L);
    Assert.assertNotNull(entity);
  }

  @Test
  public void testUploadCustomTask() {
    ResponseEntity entity = developerController.uploadCustomTask(null);
    Assert.assertNotNull(entity);
  }

  @Test
  public void testUpdateCustomTask() {
    ResponseEntity entity = developerController.updateCustomTask("sample", TaskType.ERROR, null);
    Assert.assertNotNull(entity);
  }

  @Test
  public void testRemoveCustomTask() {
    ResponseEntity entity = developerController.removeCustomTask("sample", TaskType.ERROR);
    Assert.assertNotNull(entity);
  }

  @Test
  public void testListTasks() {
    ResponseEntity entity = developerController.listTasks();
    Assert.assertNotNull(entity);
  }

  private static Workflow createWorkflow() {
    Workflow workflow = new Workflow();
    workflow.setId(1L);
    workflow.setName("a");
    return workflow;
  }

  private static WorkflowController createWorkflowController() throws Exception {
    WorkflowController workflowController = new WorkflowController();
    Field wtm = workflowController.getClass().getDeclaredField("workflowTableManager");
    wtm.setAccessible(true);
    wtm.set(workflowController, workflowTableManager);
    doReturn(createWorkflow()).when(workflowTableManager).removeWorkflow(any());
    Field jtm = workflowController.getClass().getDeclaredField("jobTableManager");
    jtm.setAccessible(true);
    jtm.set(workflowController, jobTableManager);
    Field em = workflowController.getClass().getDeclaredField("engineManager");
    em.setAccessible(true);
    em.set(workflowController, engineManager);
    return workflowController;
  }
}
