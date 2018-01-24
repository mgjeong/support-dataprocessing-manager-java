package org.edgexfoundry.support.dataprocessing.runtime.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.Workflow;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponent;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.WorkflowComponentBundleType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEditorMetadata;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSource;
import org.edgexfoundry.support.dataprocessing.runtime.db.WorkflowTableManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {WorkflowTableManager.class})
public class WorkflowControllerTest {

  private WorkflowTableManager workflowTableManager = mock(WorkflowTableManager.class);

  @Test
  public void testListWorkflows() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    Workflow sampleWorkflow = createSampleWorkflow();
    List<Workflow> workflows = new ArrayList();
    workflows.add(sampleWorkflow);
    doReturn(workflows).when(workflowTableManager).listWorkflows();
    ResponseEntity response = workflowController.listWorkflows(false, "", true, 3);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testAddOrUpdateWorkflow() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    Workflow sampleWorkflow = createSampleWorkflow();
    doReturn(sampleWorkflow).when(workflowTableManager)
        .addOrUpdateWorkflow(sampleWorkflow.getId(), sampleWorkflow);

    ResponseEntity response = workflowController
        .addOrUpdateWorkflow(sampleWorkflow.getId(), sampleWorkflow);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testGetWorkflowById() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    Workflow sampleWorkflow = createSampleWorkflow();
    doReturn(sampleWorkflow).when(workflowTableManager)
        .getWorkflow(sampleWorkflow.getId());
    doReturn(null).when(workflowTableManager).getWorkflow(500L);

    // ok - detail
    ResponseEntity response = workflowController
        .getWorkflowById(sampleWorkflow.getId(), true, 3);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

    // ok
    response = workflowController
        .getWorkflowById(sampleWorkflow.getId(), false, 3);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

    // invalid
    response = workflowController
        .getWorkflowById(500L, true, 3);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assert.assertTrue(response.getBody().toString().contains("does not exist"));
  }

  @Test
  public void testRemoveWorkflow() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    Workflow sampleWorkflow = createSampleWorkflow();
    doReturn(sampleWorkflow).when(workflowTableManager)
        .removeWorkflow(sampleWorkflow.getId());

    ResponseEntity response = workflowController.removeWorkflow(sampleWorkflow.getId(), true, true);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testListWorkflowComponentBundles() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    doReturn(Collections.emptyList()).when(workflowTableManager)
        .listWorkflowComponentBundles(any());

    for (WorkflowComponentBundleType type : WorkflowComponentBundleType.values()) {
      ResponseEntity response = workflowController.listWorkflowComponentBundles(type);
      Assert.assertNotNull(response);
      Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    ResponseEntity response = workflowController.listWorkflowComponentBundles(null);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testAddWorkflow() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    Workflow sampleWorkflow = createSampleWorkflow();
    doReturn(sampleWorkflow).when(workflowTableManager).addWorkflow(sampleWorkflow);

    ResponseEntity response = workflowController.addWorkflow(sampleWorkflow);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testSearchWorkflow() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    Workflow w1 = createSampleWorkflow(1, "a");
    Workflow w2 = createSampleWorkflow(2, "b");
    List<Workflow> workflows = new ArrayList<>();
    workflows.add(w1);
    workflows.add(w2);
    doReturn(workflows).when(workflowTableManager).listWorkflows();

    // sort name
    ResponseEntity response = workflowController.searchWorkflow("name", false, "", null, false, 3);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

    // sort time
    response = workflowController.searchWorkflow("time", false, "", null, false, 3);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

    // search string
    response = workflowController.searchWorkflow("name", false, "", "a", false, 3);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testAddWorkflowEditorMetadata() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowEditorMetadata editorMetadata = createSampleWorkflowEditorMetadata();

    doReturn(editorMetadata).when(workflowTableManager).addWorkflowEditorMetadata(editorMetadata);

    ResponseEntity response = workflowController.addWorkflowEditorMetadata(editorMetadata);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testAddOrUpdateWorkflowEditorMetadata() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowEditorMetadata editorMetadata = createSampleWorkflowEditorMetadata();

    doReturn(editorMetadata).when(workflowTableManager)
        .addOrUpdateWorkflowEditorMetadata(editorMetadata.getWorkflowId(), editorMetadata);

    ResponseEntity response = workflowController
        .addOrUpdateWorkflowEditorMetaData(editorMetadata.getWorkflowId(), editorMetadata);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testListWorkflowSources() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowSource source = new WorkflowSource();
    List<WorkflowComponent> components = new ArrayList<>();
    components.add(source);
    doReturn(components).when(workflowTableManager).listSources(1L);
    ResponseEntity response = workflowController
        .listWorkflowSources(1L);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  private WorkflowEditorMetadata createSampleWorkflowEditorMetadata() {
    WorkflowEditorMetadata editorMetadata = new WorkflowEditorMetadata();
    editorMetadata.setTimestamp(System.currentTimeMillis());
    editorMetadata.setData("{}");
    editorMetadata.setWorkflowId(1L);
    return editorMetadata;
  }

  private Workflow createSampleWorkflow() {
    return createSampleWorkflow(1L, "sample workflow");
  }

  private Workflow createSampleWorkflow(long id, String name) {
    Workflow workflow = new Workflow();
    workflow.setId(id);
    workflow.setName(name);
    return workflow;
  }

  private WorkflowController createWorkflowController() throws Exception {
    WorkflowController workflowController = new WorkflowController();
    Field wtm = workflowController.getClass().getDeclaredField("workflowTableManager");
    wtm.setAccessible(true);
    wtm.set(workflowController, workflowTableManager);
    return workflowController;
  }

}
