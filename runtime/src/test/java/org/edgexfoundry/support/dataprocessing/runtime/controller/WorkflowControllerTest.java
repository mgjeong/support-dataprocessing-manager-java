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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.controller.WorkflowController.ImportType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState.State;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.Workflow;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponent;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.WorkflowComponentBundleType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData.EngineType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEdge;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEditorMetadata;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEditorToolbar;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSink;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSource;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowStream;
import org.edgexfoundry.support.dataprocessing.runtime.db.JobTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.db.WorkflowTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.engine.Engine;
import org.edgexfoundry.support.dataprocessing.runtime.engine.EngineManager;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.FlinkEngine;
import org.edgexfoundry.support.dataprocessing.runtime.pharos.EdgeInfo;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskManager;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {WorkflowTableManager.class, JobTableManager.class, EngineManager.class,
    TaskManager.class})
public class WorkflowControllerTest {

  private static final Gson gson = new Gson();

  private WorkflowTableManager workflowTableManager = mock(WorkflowTableManager.class);
  private JobTableManager jobTableManager = mock(JobTableManager.class);
  private EngineManager engineManager = mock(EngineManager.class);

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
    WorkflowData sampleWorkflowData = createSampleWorkflowData(sampleWorkflow);
    Job sampleJob = createSampleJob(sampleWorkflowData);
    List<Job> sampleJobs = new ArrayList<>();
    sampleJobs.add(sampleJob);
    doReturn(sampleJobs).when(jobTableManager).getJobsByWorkflow(any());
    doReturn(sampleWorkflow).when(workflowTableManager).removeWorkflow(sampleWorkflow.getId());

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

  @Test
  public void testListWorkflowProcessors() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowProcessor processor = new WorkflowProcessor();
    List<WorkflowComponent> components = new ArrayList<>();
    components.add(processor);
    doReturn(components).when(workflowTableManager).listProcessors(1L);
    ResponseEntity response = workflowController
        .listWorkflowProcessors(1L);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testListWorkflowSinks() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowSink sink = new WorkflowSink();
    List<WorkflowComponent> components = new ArrayList<>();
    components.add(sink);
    doReturn(components).when(workflowTableManager).listSinks(1L);
    ResponseEntity response = workflowController
        .listWorkflowSinks(1L);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testListWorkflowEdges() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowEdge edge = new WorkflowEdge();
    List<WorkflowEdge> edges = new ArrayList<>();
    edges.add(edge);
    doReturn(edges).when(workflowTableManager).listWorkflowEdges(1L);
    ResponseEntity response = workflowController
        .listWorkflowEdges(1L);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testGetWorkflowEditorMetadataByWorkflowId() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowEditorMetadata metadata = createSampleWorkflowEditorMetadata();
    doReturn(metadata).when(workflowTableManager).getWorkflowEditorMetadata(1L);

    ResponseEntity response = workflowController.getWorkflowEditorMetadataByWorkflowId(1L);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testListWorkflowEditorToolbar() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowEditorToolbar toolbar = createSampleWorkflowEditorToolbar();
    doReturn(toolbar).when(workflowTableManager).getWorkflowEditorToolbar();

    ResponseEntity response = workflowController.listWorkflowEditorToolbar();
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testAddOrUpdateWorkflowEditorToolbar() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowEditorToolbar toolbar = createSampleWorkflowEditorToolbar();
    doReturn(toolbar).when(workflowTableManager).addOrUpdateWorkflowEditorToolbar(toolbar);

    ResponseEntity response = workflowController.addOrUpdateWorkflowEditorToolbar(toolbar);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testAddWorkflowSource() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowSource source = new WorkflowSource();
    doReturn(source).when(workflowTableManager).addWorkflowComponent(1L, source);

    ResponseEntity response = workflowController.addWorkflowSource(1L, source);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }

  @Test
  public void testGetWorkflowSourceById() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowSource source = new WorkflowSource();
    doReturn(source).when(workflowTableManager).getWorkflowComponent(1L, 1L);

    ResponseEntity response = workflowController.getWorkflowSourceById(1L, 1L);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testAddOrUpdateWorkflowSource() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowSource source = new WorkflowSource();
    doReturn(source).when(workflowTableManager).addOrUpdateWorkflowComponent(1L, 1L, source);

    ResponseEntity response = workflowController.addOrUpdateWorkflowSource(1L, 1L, source);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }

  @Test
  public void testRemoveWorkflowSource() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowSource source = new WorkflowSource();
    doReturn(source).when(workflowTableManager).removeWorkflowComponent(1L, 1L);

    ResponseEntity response = workflowController.removeWorkflowSource(1L, 1L, true);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testAddWorkflowProcessor() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowProcessor processor = new WorkflowProcessor();
    doReturn(processor).when(workflowTableManager).addWorkflowComponent(1L, processor);

    ResponseEntity response = workflowController.addWorkflowProcessor(1L, processor);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }

  @Test
  public void testGetWorkflowProcessorById() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowProcessor processor = new WorkflowProcessor();
    doReturn(processor).when(workflowTableManager).getWorkflowComponent(1L, 1L);

    ResponseEntity response = workflowController.getWorkflowProcessorById(1L, 1L);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testAddOrUpdateWorkflowProcessor() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowProcessor processor = new WorkflowProcessor();
    doReturn(processor).when(workflowTableManager).addOrUpdateWorkflowComponent(1L, 1L, processor);

    ResponseEntity response = workflowController.addOrUpdateWorkflowProcessor(1L, 1L, processor);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }

  @Test
  public void testRemoveWorkflowProcessor() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowProcessor processor = new WorkflowProcessor();
    doReturn(processor).when(workflowTableManager).removeWorkflowComponent(1L, 1L);

    ResponseEntity response = workflowController.removeWorkflowProcessor(1L, 1L, true);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testAddWorkflowSink() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowSink sink = new WorkflowSink();
    doReturn(sink).when(workflowTableManager).addWorkflowComponent(1L, sink);

    ResponseEntity response = workflowController.addWorkflowSink(1L, sink);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }

  @Test
  public void testGetWorkflowSinkById() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowSink sink = new WorkflowSink();
    doReturn(sink).when(workflowTableManager).getWorkflowComponent(1L, 1L);

    ResponseEntity response = workflowController.getWorkflowSinkById(1L, 1L);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testAddOrUpdateWorkflowSink() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowSink sink = new WorkflowSink();
    doReturn(sink).when(workflowTableManager).addOrUpdateWorkflowComponent(1L, 1L, sink);

    ResponseEntity response = workflowController.addOrUpdateWorkflowSink(1L, 1L, sink);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }

  @Test
  public void testRemoveWorkflowSink() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowSink sink = new WorkflowSink();
    doReturn(sink).when(workflowTableManager).removeWorkflowComponent(1L, 1L);

    ResponseEntity response = workflowController.removeWorkflowSink(1L, 1L, true);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testAddWorkflowEdge() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowEdge edge = new WorkflowEdge();
    doReturn(edge).when(workflowTableManager).addWorkflowEdge(1L, edge);

    ResponseEntity response = workflowController.addWorkflowEdge(1L, edge);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }

  @Test
  public void testGetWorkflowEdgeById() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowEdge edge = new WorkflowEdge();
    doReturn(edge).when(workflowTableManager).getWorkflowEdge(1L, 1L);

    ResponseEntity response = workflowController.getWorkflowEdgeById(1L, 1L);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testAddOrUpdateWorkflowEdge() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowEdge edge = new WorkflowEdge();
    doReturn(edge).when(workflowTableManager).addOrUpdateWorkflowEdge(1L, 1L, edge);

    ResponseEntity response = workflowController.addOrUpdateWorkflowEdge(1L, 1L, edge);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }

  @Test
  public void testRemoveWorkflowEdge() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowEdge edge = new WorkflowEdge();
    doReturn(edge).when(workflowTableManager).removeWorkflowEdge(1L, 1L);

    ResponseEntity response = workflowController.removeWorkflowEdge(1L, 1L);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testListStreamInfos() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowStream stream = createSampleWorkflowStream();
    List<WorkflowStream> streams = new ArrayList<>();
    streams.add(stream);
    doReturn(streams).when(workflowTableManager).listWorkflowStreams(stream.getWorkflowId());

    ResponseEntity response = workflowController.listStreamInfos(stream.getWorkflowId());
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testGetStreamInfoById() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowStream stream = createSampleWorkflowStream();
    doReturn(stream).when(workflowTableManager)
        .getWorkflowStream(stream.getWorkflowId(), stream.getId());

    ResponseEntity response = workflowController
        .getStreamInfoById(stream.getWorkflowId(), stream.getId());
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testRemoveStreamInfo() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    WorkflowStream stream = createSampleWorkflowStream();
    doReturn(stream).when(workflowTableManager)
        .removeWorkflowStream(stream.getWorkflowId(), stream.getId());

    ResponseEntity response = workflowController
        .removeStreamInfo(stream.getWorkflowId(), stream.getId());
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testExportWorkflow() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    Workflow workflow = createSampleWorkflow();
    String exportedString = "{}";

    // normal
    doReturn(workflow).when(workflowTableManager).getWorkflow(workflow.getId());
    doReturn(exportedString).when(workflowTableManager).exportWorkflow(workflow);

    ResponseEntity response = workflowController.exportWorkflow(workflow.getId());
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

    // invalid
    doThrow(new Exception("Mocked")).when(workflowTableManager).exportWorkflow(any());
    response = workflowController.exportWorkflow(workflow.getId());
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  public void testImportWorkflow() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    Workflow workflow = createSampleWorkflow();

    doReturn(workflow).when(workflowTableManager).importWorkflow(any(), any());

    // valid
    MultipartFile mockedPart = mock(MultipartFile.class);
    ObjectMapper mockedMapper = mock(ObjectMapper.class);
    Field mapperField = workflowController.getClass().getDeclaredField("mapper");
    mapperField.setAccessible(true);
    mapperField.set(workflowController, mockedMapper);
    ResponseEntity response = workflowController
        .importWorkflow(ImportType.FILE, mockedPart, null, "sample");
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

    // invalid
    response = workflowController.importWorkflow(null, null, null, "sample");
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    // test json
    response = workflowController.importWorkflow(ImportType.JSON, null, "{}", "sample");
    Assert.assertNotNull(response);
  }

  @Test
  public void testGetGroupList() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    EdgeInfo edgeInfo = mock(EdgeInfo.class);
    Field edgeInfoField = workflowController.getClass().getDeclaredField("edgeInfo");
    edgeInfoField.setAccessible(true);
    edgeInfoField.set(workflowController, edgeInfo);
    doReturn(Collections.EMPTY_LIST).when(edgeInfo).getGroupList();

    ResponseEntity response = workflowController.getGroupList();
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testGetEngineList() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    EdgeInfo edgeInfo = mock(EdgeInfo.class);
    Field edgeInfoField = workflowController.getClass().getDeclaredField("edgeInfo");
    edgeInfoField.setAccessible(true);
    edgeInfoField.set(workflowController, edgeInfo);
    List<String> engineList = new ArrayList<>();
    engineList.add("localhost:5555");
    doReturn(engineList).when(edgeInfo).getEngineList(any(), any());

    ResponseEntity response = workflowController.getEngineList("1", null);
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

    response = workflowController.getEngineList("1", "FLINK");
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

    response = workflowController.getEngineList("1", "KAPACITOR");
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testValidateWorkflow() throws Exception {
    WorkflowController workflowController = createWorkflowController();
    Workflow sampleWorkflow = createSampleWorkflow();
    doReturn(sampleWorkflow).when(workflowTableManager).getWorkflow(sampleWorkflow.getId());
    ResponseEntity response = workflowController.validateWorkflow(sampleWorkflow.getId());

    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testDeployWorkflow() throws Exception {
    WorkflowController workflowController = spy(createWorkflowController());
    Workflow workflow = createSampleWorkflow();
    WorkflowData workflowData = createSampleWorkflowData(workflow);
    Job job = createSampleJob(workflowData);
    Engine engine = mock(FlinkEngine.class);

    doReturn(engine).when(engineManager).getEngine(anyString(), anyInt(), any());
    doReturn(workflow).when(workflowTableManager).getWorkflow(workflow.getId());
    doReturn(workflowData).when(workflowTableManager).doExportWorkflow(workflow);
    doReturn(job).when(jobTableManager).addJob(any());
    doReturn(job.getState()).when(jobTableManager).updateJobState(any());

    ResponseEntity response = workflowController.deployWorkflow(workflow.getId());
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assert.assertTrue(isValidJson(response.getBody().toString()));

    // Test exceptions
    doThrow(new RuntimeException("mocked")).when(engine).run(any());
    response = workflowController.deployWorkflow(workflow.getId());
    Assert.assertTrue(isValidJson(response.getBody().toString()));

    doThrow(new RuntimeException("mocked")).when(workflowTableManager).getWorkflow(any());
    response = workflowController.deployWorkflow(workflow.getId());
    Assert.assertTrue(isValidJson(response.getBody().toString()));
  }

  @Test
  public void testStopJob() throws Exception {
    WorkflowController workflowController = spy(createWorkflowController());
    Workflow workflow = createSampleWorkflow();
    WorkflowData workflowData = createSampleWorkflowData(workflow);
    Job job = createSampleJob(workflowData);
    Engine engine = mock(FlinkEngine.class);

    doReturn(engine).when(engineManager).getEngine(anyString(), anyInt(), any());
    doReturn(job).when(jobTableManager).getJobById(any());
    doReturn(job.getState()).when(jobTableManager).updateJobState(any());

    ResponseEntity response = workflowController.stopJob(workflow.getId(), job.getId());
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assert.assertTrue(isValidJson(response.getBody().toString()));

    job.getState().setState(State.STOPPED);
    response = workflowController.stopJob(workflow.getId(), job.getId());
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assert.assertTrue(isValidJson(response.getBody().toString()));

    // test exception
    doThrow(new RuntimeException("mocked")).when(engine).stop(any());
    response = workflowController.stopJob(workflow.getId(), job.getId());
    Assert.assertTrue(isValidJson(response.getBody().toString()));

    doThrow(new RuntimeException("mocked")).when(jobTableManager).getJobById(any());
    response = workflowController.stopJob(workflow.getId(), job.getId());
    Assert.assertTrue(isValidJson(response.getBody().toString()));
  }

  @Test
  public void testMonitorJobs() throws Exception {
    WorkflowController workflowController = spy(createWorkflowController());
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

    ResponseEntity response = workflowController.monitorJobs();
    Assert.assertTrue(isValidJson(response.getBody().toString()));

    doThrow(new RuntimeException("Mocked")).when(jobTableManager).getJobs();
    response = workflowController.monitorJobs();
    Assert.assertTrue(isValidJson(response.getBody().toString()));
  }

  @Test
  public void testMonitorJob() throws Exception {
    WorkflowController workflowController = spy(createWorkflowController());
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

    ResponseEntity response = workflowController.monitorJob(workflowA.getId());
    Assert.assertTrue(isValidJson(response.getBody().toString()));

    doThrow(new RuntimeException("Mocked")).when(jobTableManager).getJobsByWorkflow(any());
    response = workflowController.monitorJob(workflowA.getId());
    Assert.assertTrue(isValidJson(response.getBody().toString()));
  }

  @Test
  public void testMonitorJobDetails() throws Exception {
    WorkflowController workflowController = spy(createWorkflowController());
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

    ResponseEntity response = workflowController.monitorJobDetails(workflowA.getId());
    Assert.assertTrue(isValidJson(response.getBody().toString()));

    doThrow(new RuntimeException("Mocked")).when(jobTableManager).getJobsByWorkflow(any());
    response = workflowController.monitorJobDetails(workflowA.getId());
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

  private WorkflowStream createSampleWorkflowStream() {
    WorkflowStream stream = new WorkflowStream();
    stream.setWorkflowId(1L);
    stream.setDescription("");
    stream.setStreamId("stream-id");
    stream.setComponentId(1L);
    return stream;
  }

  private WorkflowEditorToolbar createSampleWorkflowEditorToolbar() {
    WorkflowEditorToolbar toolbar = new WorkflowEditorToolbar();
    toolbar.setUserId(1L);
    toolbar.setData("{}");
    toolbar.setTimestamp(System.currentTimeMillis());
    return toolbar;
  }

  private WorkflowEditorMetadata createSampleWorkflowEditorMetadata() {
    WorkflowEditorMetadata editorMetadata = new WorkflowEditorMetadata();
    editorMetadata.setTimestamp(System.currentTimeMillis());
    editorMetadata.setData("{}");
    editorMetadata.setWorkflowId(1L);
    return editorMetadata;
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
    Field jtm = workflowController.getClass().getDeclaredField("jobTableManager");
    jtm.setAccessible(true);
    jtm.set(workflowController, jobTableManager);
    Field em = workflowController.getClass().getDeclaredField("engineManager");
    em.setAccessible(true);
    em.set(workflowController, engineManager);
    return workflowController;
  }

  @Test
  public void testAddCustomTask() throws Exception {
    WorkflowController workflowController = spy(new WorkflowController());
    TaskManager taskManager = mock(TaskManager.class);
    WhiteboxImpl.setInternalState(workflowController, "taskManager", taskManager);
    // test successful validation
    MultipartFile mocked = mock(MultipartFile.class);
    doReturn(false).when(mocked).isEmpty();
    doReturn("sample.jar").when(mocked).getOriginalFilename();
    doReturn("sample.jar").when(mocked).getName();
    doReturn("hello".getBytes()).when(mocked).getBytes();

    ResponseEntity responseEntity = workflowController.addCustomTask(mocked);
    Assert.assertTrue(responseEntity.getBody().toString().contains(mocked.getOriginalFilename()));

    // test exception
    doThrow(new Exception("Mocked")).when(taskManager).addCustomTask(any(), any());
    responseEntity = workflowController.addCustomTask(mocked);
    Assert.assertTrue(responseEntity.getBody().toString().contains("error"));

    // test invalid param
    responseEntity = workflowController.addCustomTask(null);
    Assert.assertTrue(responseEntity.getBody().toString().contains("error"));
  }

  @Test
  public void testUpdateCustomTask() throws Exception {
    WorkflowController workflowController = spy(new WorkflowController());
    TaskManager taskManager = mock(TaskManager.class);
    WhiteboxImpl.setInternalState(workflowController, "taskManager", taskManager);
    // test successful validation
    MultipartFile mocked = mock(MultipartFile.class);
    doReturn(false).when(mocked).isEmpty();
    doReturn("sample.jar").when(mocked).getOriginalFilename();
    doReturn("sample.jar").when(mocked).getName();
    doReturn("hello".getBytes()).when(mocked).getBytes();

    ResponseEntity responseEntity = workflowController
        .updateCustomTask("A", TaskType.INVALID, mocked);
    Assert.assertTrue(responseEntity.getBody().toString().contains("Success"));

    // test exception
    doThrow(new Exception("Mocked")).when(taskManager).updateCustomTask(any(), any(), any(), any());
    responseEntity = workflowController.updateCustomTask("A", TaskType.INVALID, mocked);
    Assert.assertTrue(responseEntity.getBody().toString().contains("error"));

    // test invalid param
    responseEntity = workflowController.updateCustomTask(null, null, mocked);
    Assert.assertTrue(responseEntity.getBody().toString().contains("error"));

    // test invalid param
    responseEntity = workflowController.updateCustomTask("A", TaskType.INVALID, null);
    Assert.assertTrue(responseEntity.getBody().toString().contains("error"));
  }

  @Test
  public void testRemoveCustomTask() throws Exception {
    WorkflowController workflowController = spy(new WorkflowController());
    TaskManager taskManager = mock(TaskManager.class);
    WhiteboxImpl.setInternalState(workflowController, "taskManager", taskManager);
    // test successful validation

    ResponseEntity responseEntity = workflowController.removeCustomTask("A", TaskType.INVALID);
    Assert.assertTrue(responseEntity.getBody().toString().contains("Success"));

    // test exception
    doThrow(new RuntimeException("Mocked")).when(taskManager).removeCustomTask(any(), any());
    responseEntity = workflowController.removeCustomTask("A", TaskType.INVALID);
    Assert.assertTrue(responseEntity.getBody().toString().contains("error"));

    // test invalid param
    responseEntity = workflowController.removeCustomTask(null, null);
    Assert.assertTrue(responseEntity.getBody().toString().contains("error"));

  }
}
