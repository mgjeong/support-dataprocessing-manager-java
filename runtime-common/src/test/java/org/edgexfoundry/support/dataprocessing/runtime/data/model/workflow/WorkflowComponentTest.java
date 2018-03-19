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
import java.util.NoSuchElementException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

public class WorkflowComponentTest {

  @Test
  public void testGetterAndSetter() {
    WorkflowComponent component = new WorkflowComponent();
    component.setId(1L);
    component.setWorkflowId(1L);
    component.setWorkflowComponentBundleId(1L);
    component.setName("sample");
    component.setDescription("sample component");
    component.setBundleName("bundleName");
    component.setBundleSubType("bundleSubType");
    component.setEngineType("FLINK");
    component.setPath("path");
    component.setClassname("classname");
    component.setConfigStr("{}");

    Assert.assertEquals(1L, component.getId().longValue());
    Assert.assertEquals(1L, component.getWorkflowId().longValue());
    Assert.assertEquals(1L, component.getWorkflowComponentBundleId().longValue());
    Assert.assertEquals("sample", component.getName());
    Assert.assertEquals("sample component", component.getDescription());
    Assert.assertEquals("FLINK", component.getEngineType());
    Assert.assertEquals("path", component.getPath());
    Assert.assertEquals("classname", component.getClassname());
    Assert.assertEquals(0, component.getConfig().getProperties().size());
    Assert.assertEquals("bundleName", component.getBundleName());
    Assert.assertEquals("bundleSubType", component.getBundleSubType());

    WorkflowComponent.Config config = new WorkflowComponent.Config();
    component.setConfig(config);
    Assert.assertEquals(0, component.getConfig().getProperties().size());
    Assert.assertEquals("{\"properties\":{}}", component.getConfigStr());
  }

  @Test
  public void testConfig() {
    WorkflowComponent component = new WorkflowComponent();
    component.addConfig("sampleString", "hello world");
    component.addConfig("sampleInt", 1);
    component.addConfig("sampleDouble", 3.14);

    Assert.assertEquals("hello world", component.getConfig("sampleString"));
    Assert.assertEquals(1, (int) component.getConfig("sampleInt"));
    Assert.assertEquals(3.14, component.getConfig("sampleDouble"), 0.01);
  }

  @Test
  public void testInvalidConfig() throws Exception {
    WorkflowComponent component = new WorkflowComponent();

    WorkflowComponent.Config config = new WorkflowComponent.Config();
    config.setProperties(new HashMap<>());
    component.setConfig(config);
    try {
      component.getConfig("invalidKey");
      Assert.fail("Should not reach here.");
    } catch (NoSuchElementException e) {
      // Success
    }

    try {
      component.setConfig(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // Success
    }

    try {
      component.setConfigStr(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // Success
    }

    // Make config null
    Field configField = component.getClass().getDeclaredField("config");
    configField.setAccessible(true);
    configField.set(component, null);
    Assert.assertNull(component.getConfig("invalidKey"));

    ObjectMapper objectMapper = Mockito.spy(new ObjectMapper());
    Mockito.when(objectMapper.writeValueAsString(Mockito.any()))
        .thenThrow(new JsonProcessingException("JsonProcessingException mocked!") {
        });
    Whitebox.setInternalState(component, "mapper", objectMapper);
    try {
      component.addConfig("sample", 1);
      component.getConfigStr();
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // Success
    }
  }
}
