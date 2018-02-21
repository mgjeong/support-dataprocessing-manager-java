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
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSink;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class SinkVertexTest {

  private static final Long TEST_ID = 1234L;

  private SinkVertex initialize(Map<String, String> info) throws Exception {
    WorkflowSink sink = new WorkflowSink();
    sink.setId(TEST_ID);

    if (info != null) {
      Map<String, Object> properties = sink.getConfig().getProperties();
      properties.putAll(info);
    }

    DataStream dataStream = Mockito.mock(DataStream.class);
    DataStreamSink dataStreamSink = Mockito.mock(DataStreamSink.class);

    Mockito.when(dataStream.addSink(any())).thenReturn(dataStreamSink);
    Mockito.when(dataStreamSink.setParallelism(anyInt())).thenReturn(null);

    SinkVertex res = new SinkVertex(sink);
    res.setInflux(dataStream);

    return res;
  }

  @Test
  public void testGetId() throws Exception {
    SinkVertex testSink = initialize(null);
    Assert.assertEquals(testSink.getId(), TEST_ID);
  }

  @Test
  public void testServeZmq() throws Exception {
    Map<String, String> properties = new HashMap<>();
    properties.put("dataType", "zmq");
    properties.put("dataSink", "localhost:0:topic");

    SinkVertex testSink = initialize(properties);
    testSink.serve();
  }

  @Test
  public void testServeEzmq() throws Exception {
    Map<String, String> properties = new HashMap<>();
    properties.put("dataType", "ezmq");
    properties.put("dataSink", "localhost:0:topic");

    SinkVertex testSink = initialize(properties);
    testSink.serve();
  }

  @Test
  public void testServeWs() throws Exception {
    Map<String, String> properties = new HashMap<>();
    properties.put("dataType", "ws");
    properties.put("dataSink", "localhost:0");

    SinkVertex testSink = initialize(properties);
    testSink.serve();
  }

  @Test
  public void testServeFile() throws Exception {
    Map<String, String> properties = new HashMap<>();
    properties.put("dataType", "f");
    properties.put("dataSink", "filename");

    SinkVertex testSink = initialize(properties);
    testSink.serve();
  }

  @Test
  public void testServeMongo() throws Exception {
    Map<String, String> properties = new HashMap<>();
    properties.put("dataType", "mongodb");
    properties.put("dataSink", "localhost:0");
    properties.put("name", "db:collection");

    SinkVertex testSink = initialize(properties);
    testSink.serve();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testServeInvalidSink() throws Exception {
    Map<String, String> properties = new HashMap<>();
    properties.put("dataType", "sinkNotExist");
    properties.put("dataSink", "localhost:0");

    SinkVertex testSink = initialize(properties);
    testSink.serve();
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testServeWithShortProperties() throws Exception {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("dataType and dataSink");
    SinkVertex testSink = initialize(null);
    testSink.serve();
  }

  @Test
  public void testServeWithEmptyProperties() throws Exception {
    Map<String, String> properties = new HashMap<>();
    properties.put("dataType", "");
    properties.put("dataSink", "");

    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("Empty");
    SinkVertex testSink = initialize(properties);
    testSink.serve();
  }

  @Test
  public void testServeWithNullProperties() throws Exception {
    Map<String, String> properties = new HashMap<>();
    properties.put("dataType", null);
    properties.put("dataSink", null);

    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("Null");
    SinkVertex testSink = initialize(properties);
    testSink.serve();
  }

  @Test
  public void testSetInflux() throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    DataStream dataStream = Mockito.mock(DataStream.class);

    SinkVertex testSink = new SinkVertex(null);
    DataStream stream = env.fromElements(dataStream);
    testSink.setInflux(stream);
    testSink.setInflux(stream);
  }
}
