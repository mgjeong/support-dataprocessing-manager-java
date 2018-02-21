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
import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEdge.StreamGrouping;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowStream.Grouping;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

public class WorkflowEdgeTest {

  @Test
  public void testSetterAndGetter() {
    WorkflowEdge edge = new WorkflowEdge();
    edge.setId(1L);
    edge.setWorkflowId(1L);
    edge.setFromId(1L);
    edge.setToId(1L);
    edge.setStreamGroupingsStr("[]");

    Assert.assertEquals(1L, edge.getId().longValue());
    Assert.assertEquals(1L, edge.getWorkflowId().longValue());
    Assert.assertEquals(1L, edge.getFromId().longValue());
    Assert.assertEquals(1L, edge.getToId().longValue());
    Assert.assertEquals("[]", edge.getStreamGroupingsStr());

    List<StreamGrouping> streamGroupingList = new ArrayList<>();
    StreamGrouping streamGrouping = new StreamGrouping();
    streamGrouping.setStreamId(1L);
    streamGrouping.setGrouping(Grouping.SHUFFLE);
    streamGrouping.setFields(new ArrayList<>());
    Assert.assertEquals(1L, streamGrouping.getStreamId().longValue());
    Assert.assertEquals(Grouping.SHUFFLE, streamGrouping.getGrouping());
    Assert.assertEquals(0, streamGrouping.getFields().size());

    streamGroupingList.add(streamGrouping);
    edge.setStreamGroupings(streamGroupingList);
    Assert.assertEquals(1, edge.getStreamGroupings().size());
  }

  @Test
  public void testInvalidSetter() {
    WorkflowEdge edge = new WorkflowEdge();
    try {
      edge.setStreamGroupings(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // success
    }

    try {
      edge.setStreamGroupingsStr(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // success
    }

    try {
      edge.setStreamGroupingsStr("invalid");
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // success
    }
  }

  @Test
  public void testInvalidGetter() throws Exception {
    WorkflowEdge edge = new WorkflowEdge();
    List<StreamGrouping> streamGroupingList = new ArrayList<>();
    StreamGrouping streamGrouping = new StreamGrouping();
    streamGrouping.setStreamId(1L);
    streamGrouping.setGrouping(Grouping.SHUFFLE);
    streamGrouping.setFields(new ArrayList<>());
    streamGroupingList.add(streamGrouping);
    edge.setStreamGroupings(streamGroupingList);

    ObjectMapper objectMapper = Mockito.spy(new ObjectMapper());
    Mockito.when(objectMapper.writeValueAsString(Mockito.any()))
        .thenThrow(new JsonProcessingException("JsonProcessingException mocked!") {
        });
    Whitebox.setInternalState(edge, "mapper", objectMapper);
    try {
      edge.getStreamGroupingsStr();
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // Success
    }
  }
}
