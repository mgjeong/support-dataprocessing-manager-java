/*******************************************************************************
 * Copyright 2017 Samsung Electronics All Rights Reserved.
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

package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.ezmq;

import org.edgexfoundry.ezmq.EZMQAPI;
import org.edgexfoundry.ezmq.EZMQErrorCode;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.junit.Test;

public class EZMQSinkTest {

  private static final String EZMQ_HOST = "localhost";
  private static final int EZMQ_PORT = 5599;

  @Test
  public void testInvoke() throws Exception {
    EZMQAPI instance = EZMQAPI.getInstance();
    EzmqSink sink = new EzmqSink(EZMQ_PORT);
    sink.open(null);

    sink.onStartCB(EZMQErrorCode.EZMQ_OK);
    sink.onStopCB(EZMQErrorCode.EZMQ_OK);
    sink.onErrorCB(EZMQErrorCode.EZMQ_OK);

    sink.invoke(DataSet.create("{}"));
    sink.close();
  }

  @Test
  public void testOpenClose() throws Exception {
    EzmqSink sink = new EzmqSink(EZMQ_PORT);
    try {
      sink.open(null);
      Thread.sleep(50L);
    } finally {
      sink.close();
    }
  }

}
