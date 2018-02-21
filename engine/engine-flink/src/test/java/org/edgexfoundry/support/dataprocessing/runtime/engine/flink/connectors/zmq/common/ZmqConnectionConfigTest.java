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
package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq.common;

import org.junit.Assert;
import org.junit.Test;

public class ZmqConnectionConfigTest {

  @Test
  public void testConstructor() {
    ZmqConnectionConfig config = new ZmqConnectionConfig("localhost", 5555, 1);
    Assert.assertNotNull(config);
    Assert.assertEquals("tcp://localhost:5555", config.getConnectionAddress());
    Assert.assertEquals(1, config.getIoThreads());
  }

  @Test
  public void testBuilder() {
    ZmqConnectionConfig.Builder builder = new ZmqConnectionConfig.Builder();
    builder.setHost("localhost");
    builder.setPort(5555);
    builder.setIoThreads(1);
    ZmqConnectionConfig config = builder.build();

    Assert.assertNotNull(config);
    Assert.assertEquals("tcp://localhost:5555", config.getConnectionAddress());
    Assert.assertEquals(1, config.getIoThreads());
  }
}
