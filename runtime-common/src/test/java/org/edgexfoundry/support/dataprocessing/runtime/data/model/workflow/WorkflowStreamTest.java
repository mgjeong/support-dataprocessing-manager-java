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
import java.util.ArrayList;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowStream.SchemaType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

public class WorkflowStreamTest {

  @Test
  public void testSetterAndGetter() {
    WorkflowStream stream = new WorkflowStream();
    stream.setId(1L);
    stream.setComponentId(1L);
    stream.setStreamId("streamId");
    stream.setDescription("description");
    stream.setWorkflowId(1L);
    stream.setFieldsStr("[]");

    Assert.assertEquals(1L, stream.getId().longValue());
    Assert.assertEquals(1L, stream.getComponentId().longValue());
    Assert.assertEquals("streamId", stream.getStreamId());
    Assert.assertEquals("description", stream.getDescription());
    Assert.assertEquals(1L, stream.getWorkflowId().longValue());
    Assert.assertEquals("[]", stream.getFieldsStr());

    WorkflowStream.Field field = new WorkflowStream.Field();
    field.setName("field");
    field.setOptional(false);
    field.setType(SchemaType.STRING);

    Assert.assertEquals("field", field.getName());
    Assert.assertEquals(false, field.isOptional());
    Assert.assertEquals(SchemaType.STRING, field.getType());

    stream.addField(field);
    Assert.assertEquals(1, stream.getFields().size());
    stream.setFields(new ArrayList<>());
    Assert.assertEquals(0, stream.getFields().size());
  }

  @Test
  public void testInvalidSetter() throws Exception {
    WorkflowStream stream = new WorkflowStream();

    try {
      stream.setWorkflowId(null);
      Assert.fail("Should not reach here");
    } catch (RuntimeException e) {
      // success
    }

    try {
      stream.setFieldsStr(null);
      Assert.fail("Should not reach here");
    } catch (RuntimeException e) {
      // success
    }

    try {
      stream.setFields(null);
      Assert.fail("Should not reach here");
    } catch (RuntimeException e) {
      // success
    }

    try {
      stream.addField(null);
      Assert.fail("Should not reach here");
    } catch (RuntimeException e) {
      // success
    }

    try {
      stream.setFieldsStr("invalid");
      Assert.fail("Should not reach here");
    } catch (RuntimeException e) {
      // success
    }

    ObjectMapper objectMapper = Mockito.spy(new ObjectMapper());
    Mockito.when(objectMapper.writeValueAsString(Mockito.any()))
        .thenThrow(new JsonProcessingException("JsonProcessingException mocked!") {
        });
    Whitebox.setInternalState(stream, "mapper", objectMapper);
    try {
      WorkflowStream.Field field = new WorkflowStream.Field();
      field.setName("field");
      field.setOptional(false);
      field.setType(SchemaType.STRING);
      stream.addField(field);
      stream.getFieldsStr();
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // Success
    }
  }

  @Test
  public void testSchemaType() {
    SchemaType string = SchemaType.STRING;
    Assert.assertEquals(String.class, string.getJavaType());
  }
}
