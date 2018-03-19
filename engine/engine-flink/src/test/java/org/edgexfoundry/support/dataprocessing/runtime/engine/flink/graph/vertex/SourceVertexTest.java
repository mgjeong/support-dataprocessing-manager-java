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
package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.vertex;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import java.util.HashMap;
import java.util.Map;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSource;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class SourceVertexTest {

  private static final Long TEST_ID = 1234L;
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private SourceVertex initialize(Map<String, String> info) {
    WorkflowSource source = new WorkflowSource();
    source.setId(TEST_ID);

    if (info != null) {
      Map<String, Object> properties = source.getConfig().getProperties();
      properties.putAll(info);
    }

    SourceVertex res = new SourceVertex(mockEnv(), source);
    return res;
  }

  private StreamExecutionEnvironment mockEnv() {
    StreamExecutionEnvironment env = Mockito.mock(StreamExecutionEnvironment.class);
    DataStreamSource dataStreamSource = Mockito.mock(DataStreamSource.class);
    Mockito.when(env.addSource(any(), any(), any())).thenReturn(dataStreamSource);
    Mockito.when(env.addSource(any())).thenReturn(dataStreamSource);
    Mockito.when(dataStreamSource.setParallelism(anyInt())).thenReturn(null);
    return env;
  }

  @Test
  public void testGetId() {
    SourceVertex testSource = initialize(null);
    Assert.assertEquals(testSource.getId(), TEST_ID);
  }

  @Test
  public void testServeZmq() throws Exception {
    Map<String, String> properties = new HashMap<>();
    properties.put("dataType", "zmq");
    properties.put("dataSource", "localhost:0:topic");

    SourceVertex testSource = initialize(properties);
    testSource.serve();
  }

  @Test
  public void testServeEzmq() throws Exception {
    Map<String, String> properties = new HashMap<>();
    properties.put("dataType", "ezmq");
    properties.put("dataSource", "localhost:0:topic1");

    SourceVertex testSource = initialize(properties);
    testSource.serve();

    properties.put("dataSource", "localhost:0");
    testSource = initialize(properties);
    testSource.serve();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testServeInvalidSource() throws Exception {
    Map<String, String> properties = new HashMap<>();
    properties.put("dataType", "sourceNotExist");
    properties.put("dataSource", "localhost:0:topic1, topic2");

    SourceVertex testSource = initialize(properties);
    testSource.serve();
  }

  @Test
  public void testServeWithShortProperties() throws Exception {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("dataType and dataSource");
    SourceVertex testSource = initialize(null);
    testSource.serve();
  }

  @Test
  public void testServeWithEmptyProperties() throws Exception {
    Map<String, String> properties = new HashMap<>();
    properties.put("dataType", "");
    properties.put("dataSource", "");

    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("Empty");
    SourceVertex testSource = initialize(properties);
    testSource.serve();
  }

  @Test
  public void testServeWithNullProperties() throws Exception {
    Map<String, String> properties = new HashMap<>();
    properties.put("dataType", null);
    properties.put("dataSource", null);

    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("Null");
    SourceVertex testSource = initialize(properties);
    testSource.serve();
  }

  @Test
  public void testSetInflux() {
    DataStream<DataSet> influx = Mockito.mock(DataStream.class);
    SourceVertex testSource = initialize(null);
    testSource.setInflux(influx);
  }
}
