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

package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData.EngineType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

public class WorkflowDataTest {

  @Test
  public void testSetterAndGetter() {
    WorkflowData workflowData = new WorkflowData();
    workflowData.setWorkflowId(1L);
    workflowData.setWorkflowName("sample");
    workflowData.setConfig(new HashMap<>());

    WorkflowEditorMetadata metadata = makeWorkflowEditorMetadata();
    workflowData.setWorkflowEditorMetadata(metadata);

    workflowData.setSources(new ArrayList<>());
    workflowData.setSinks(new ArrayList<>());
    workflowData.setProcessors(new ArrayList<>());
    workflowData.setEdges(new ArrayList<>());

    // Getter
    Assert.assertEquals(1L, workflowData.getWorkflowId().longValue());
    Assert.assertEquals("sample", workflowData.getWorkflowName());
    Assert.assertEquals("{}", workflowData.getConfigStr());
    Assert.assertEquals(0, workflowData.getConfig().size());
    Assert.assertEquals(metadata, workflowData.getWorkflowEditorMetadata());
    Assert.assertEquals(0, workflowData.getSources().size());
    Assert.assertEquals(0, workflowData.getSinks().size());
    Assert.assertEquals(0, workflowData.getProcessors().size());
    Assert.assertEquals(0, workflowData.getEdges().size());
    Assert.assertEquals(null, workflowData.getEngineType());

    Map<String, Object> config = new HashMap<>();
    config.put("targetHost", "localhost");
    workflowData.setConfig(config);
    Assert.assertEquals(1, workflowData.getConfig().size());
    Assert.assertTrue(!workflowData.getConfigStr().isEmpty());
  }

  @Test
  public void testInvalidSetter() {
    WorkflowData workflowData = new WorkflowData();
    try {
      workflowData.setWorkflowId(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // success
    }

    try {
      workflowData.setConfig(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // success
    }

    try {
      workflowData.setSources(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // success
    }

    try {
      workflowData.setSinks(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // success
    }

    try {
      workflowData.setProcessors(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // success
    }

    try {
      workflowData.setEdges(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // success
    }

    try {
      workflowData.setWorkflowEditorMetadata(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // success
    }
  }

  @Test
  public void testEngineType() {
    WorkflowData workflowData = new WorkflowData();
    List<WorkflowProcessor> processors = new ArrayList<>();
    WorkflowProcessor query = new WorkflowProcessor();
    query.setEngineType("KAPACITOR");
    processors.add(query);
    workflowData.setProcessors(processors);
    Assert.assertEquals(EngineType.KAPACITOR, workflowData.getEngineType());

    WorkflowProcessor svm = new WorkflowProcessor();
    svm.setEngineType("FLINK");
    processors.add(svm);
    workflowData.setProcessors(processors);
    Assert.assertEquals(EngineType.MULTI, workflowData.getEngineType());

    processors.clear();
    processors.add(svm);
    Assert.assertEquals(EngineType.FLINK, workflowData.getEngineType());

    processors.add(query);
    Assert.assertEquals(EngineType.MULTI, workflowData.getEngineType());

    processors.clear();
    WorkflowProcessor unknown = new WorkflowProcessor();
    unknown.setEngineType("unknown");
    processors.add(unknown);
    Assert.assertEquals(EngineType.UNKNOWN, workflowData.getEngineType());
  }

  @Test
  public void testInvalidGetter() throws Exception {
    WorkflowData workflowData = new WorkflowData();

    // Mock mapper
    ObjectMapper objectMapper = Mockito.spy(new ObjectMapper());
    Mockito.when(objectMapper.writeValueAsString(Mockito.any()))
        .thenThrow(new JsonProcessingException("JsonProcessingException mocked!") {
        });
    Whitebox.setInternalState(workflowData, "mapper", objectMapper);
    try {
      Map<String, Object> config = new HashMap<>();
      config.put("targetHost", "localhost");
      workflowData.setConfig(config);
      workflowData.getConfigStr();
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // Success
    }
  }

  @Test
  public void testToGson() {
    WorkflowData workflowData = new WorkflowData();
    workflowData.setWorkflowId(1L);
    workflowData.setWorkflowName("sample");
    workflowData.setConfig(new HashMap<>());
    String json = new Gson().toJson(workflowData);
    Assert.assertNotNull(json);
  }

  private WorkflowEditorMetadata makeWorkflowEditorMetadata() {
    WorkflowEditorMetadata metadata = new WorkflowEditorMetadata();
    metadata.setWorkflowId(1L);
    metadata.setData("{}");
    metadata.setTimestamp(System.currentTimeMillis());
    return metadata;
  }
}
