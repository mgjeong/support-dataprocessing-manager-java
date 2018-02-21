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
package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq;

import java.nio.charset.Charset;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq.common.ZmqConnectionConfig;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq.common.ZmqConnectionConfig.Builder;
import org.junit.Test;

public class ZmqSinkTest {

  @Test
  public void testConstructor() {
    Builder builder = new Builder();
    builder.setHost("localhost").setPort(5588).setIoThreads(1);
    ZmqConnectionConfig config = builder.build();
    new ZmqSink(config, "topic", new SimpleStringSchema(Charset.defaultCharset()));
  }

  @Test
  public void testConnection() throws Exception {
    ZmqConnectionConfig.Builder builder = new ZmqConnectionConfig.Builder();
    builder.setHost("localhost").setPort(5588).setIoThreads(1);
    ZmqConnectionConfig config = builder.build();
    ZmqSink sink = new ZmqSink(config, "topic", new SimpleStringSchema(Charset.defaultCharset()));
    sink.open(null);
    sink.close();
  }

  @Test(timeout = 3000L)
  public void testInvoke() throws Exception {
    ZmqConnectionConfig.Builder builder = new ZmqConnectionConfig.Builder();
    builder.setHost("localhost").setPort(5588).setIoThreads(1);
    ZmqConnectionConfig config = builder.build();
    ZmqSink sink = new ZmqSink(config, "topic", new SimpleStringSchema(Charset.defaultCharset()));
    sink.open(null);
    sink.invoke("Hello World!");
    sink.close();
  }
}
