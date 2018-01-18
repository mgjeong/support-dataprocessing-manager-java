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

import java.util.ArrayList;
import java.util.List;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.edgexfoundry.domain.core.Event;
import org.edgexfoundry.domain.core.Reading;
import org.edgexfoundry.ezmq.EZMQAPI;
import org.edgexfoundry.ezmq.EZMQCallback;
import org.edgexfoundry.ezmq.EZMQErrorCode;
import org.edgexfoundry.ezmq.EZMQPublisher;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EzmqSink extends RichSinkFunction<DataSet> implements EZMQCallback {

  private static final Logger LOGGER = LoggerFactory.getLogger(EzmqSink.class);

  private final int port;

  private EZMQAPI ezmqApi = null;
  private EZMQPublisher ezmqPublisher = null;

  public EzmqSink(int port) {
    this.port = port;
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);

    this.ezmqApi = EZMQAPI.getInstance();
    this.ezmqApi.initialize();
    LOGGER.info("EZMQ API initialized.");

    this.ezmqPublisher = new EZMQPublisher(this.port, this);
    EZMQErrorCode ezmqErrorCode = this.ezmqPublisher.start();
    if (ezmqErrorCode != EZMQErrorCode.EZMQ_OK) {
      throw new RuntimeException(
          String.format("Failed to start EZMQPublisher. [ErrorCode=%s]", ezmqErrorCode));
    }
  }

  @Override
  public void invoke(DataSet dataSet) throws Exception {
    if (this.ezmqPublisher != null) {
      Event event = makeEvent(dataSet);
      this.ezmqPublisher.publish(event);
    }
  }

  @Override
  public void close() throws Exception {
    super.close();

    if (this.ezmqPublisher != null) {
      this.ezmqPublisher.stop();
      LOGGER.info("EZMQ Publisher stopped.");
    }

    if (this.ezmqApi != null) {
      this.ezmqApi.terminate();
      LOGGER.info("EZMQ API terminated.");
    }
  }

  private Event makeEvent(DataSet dataSet) {
    //TODO: Convert from stream data to event accordingly.
    Reading reading = new Reading();
    reading.setName("DPFW");
    reading.setValue(dataSet.getStreamedRecord().toString());
    reading.setId("DPFW-01");
    reading.setOrigin(System.currentTimeMillis());
    reading.setPushed(reading.getOrigin());

    List<Reading> readings = new ArrayList();
    readings.add(reading);
    Event event = new Event("EzmqSink", readings);
    event.setCreated(0);
    event.setModified(0);
    event.setId("DPFW-01");
    event.markPushed(System.currentTimeMillis());
    event.setOrigin(event.getPushed());

    return event;
  }

  @Override
  public void onStartCB(EZMQErrorCode ezmqErrorCode) {
    LOGGER.info("EZMQ publisher started. [ErrorCode={}]", ezmqErrorCode);
  }

  @Override
  public void onStopCB(EZMQErrorCode ezmqErrorCode) {
    LOGGER.info("EZMQ publisher stopped. [ErrorCode={}]", ezmqErrorCode);
  }

  @Override
  public void onErrorCB(EZMQErrorCode ezmqErrorCode) {
    LOGGER.info("EZMQ publisher error occurred. [ErrorCode={}]", ezmqErrorCode);
  }
}
