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
import java.lang.reflect.Field;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

public class WorkflowTest {

  @Test
  public void testGetterAndSetter() {
    Workflow workflow = new Workflow();
    workflow.setId(1L);
    workflow.setName("testWorkflow");
    workflow.setConfigStr("{}");

    Assert.assertEquals(1L, workflow.getId().longValue());
    Assert.assertEquals("testWorkflow", workflow.getName());
    Assert.assertEquals("{}", workflow.getConfigStr());

    workflow.setConfig(new HashMap<>());
    Assert.assertEquals(0, workflow.getConfig().size());
  }

  @Test
  public void testConfig() throws Exception {
    Workflow workflow = new Workflow();

    workflow.addConfig("sampleString", "hello world");
    workflow.addConfig("sampleInt", 1);
    workflow.addConfig("sampleDouble", 3.14);

    Assert.assertEquals("hello world", workflow.getConfig("sampleString"));
    Assert.assertEquals(1, (int) workflow.getConfig("sampleInt"));
    Assert.assertEquals(3.14, workflow.getConfig("sampleDouble"), 0.005);
    Assert.assertNull(workflow.getConfig("invalidKey"));

    Assert.assertTrue(workflow.getConfigStr().contains("hello world"));

    // Make config null
    Field configField = workflow.getClass().getDeclaredField("config");
    configField.setAccessible(true);
    configField.set(workflow, null);
    Assert.assertNull(workflow.getConfig("invalidKey"));
  }

  @Test
  public void testInvalidConfig() throws Exception {
    Workflow workflow = new Workflow();
    try {
      workflow.setConfigStr("invalidconfig");
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // Success
    }

    try {
      workflow.setConfigStr(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // Success
    }

    try {
      workflow.setConfig(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // Success
    }

    // Mock mapper
    ObjectMapper objectMapper = Mockito.spy(new ObjectMapper());
    Mockito.when(objectMapper.writeValueAsString(Mockito.any()))
        .thenThrow(new JsonProcessingException("JsonProcessingException mocked!") {
        });
    Whitebox.setInternalState(workflow, "mapper", objectMapper);
    try {
      workflow.addConfig("sample", 1);
      workflow.getConfigStr();
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // Success
    }
  }
}
