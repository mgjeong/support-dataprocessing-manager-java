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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.edgexfoundry.domain.core.Event;
import org.edgexfoundry.domain.core.Reading;
import org.edgexfoundry.ezmq.EZMQAPI;
import org.edgexfoundry.ezmq.EZMQErrorCode;
import org.edgexfoundry.ezmq.EZMQSubscriber;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EzmqSource extends RichSourceFunction<DataSet> implements
    EZMQSubscriber.EZMQSubCallback {

  private static final Logger LOGGER = LoggerFactory.getLogger(EzmqSource.class);

  private final String host;
  private final int port;
  private final String topics;

  private EZMQAPI ezmqApi = null;
  private EZMQSubscriber ezmqSubscriber = null;

  private SourceContext<DataSet> sourceContext = null;

  private transient Object waitLock;
  private transient boolean running = false;

  public EzmqSource(String host, int port) {
    this.host = host;
    this.port = port;
    this.topics = null;
  }

  public EzmqSource(String host, int port, String topics) {
    this.host = host;
    this.port = port;
    this.topics = topics;
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);

    this.waitLock = new Object();

    this.ezmqApi = EZMQAPI.getInstance();
    this.ezmqApi.initialize();
    LOGGER.info("EZMQ API initialized.");

    this.ezmqSubscriber = new EZMQSubscriber(this.host, this.port, this);
    EZMQErrorCode ezmqErrorCode = this.ezmqSubscriber.start();

    if (ezmqErrorCode != EZMQErrorCode.EZMQ_OK) {
      throw new RuntimeException(
          String.format("Failed to start EZMQ subscriber. [ErrorCode=%s]", ezmqErrorCode));
    }

    if (this.topics != null) {
      String[] topicList = topics.replaceAll("\\s", "").split(",");
      for (String topic : topicList) {
        ezmqErrorCode = this.ezmqSubscriber.subscribe(topic);
        if (ezmqErrorCode != EZMQErrorCode.EZMQ_OK) {
          throw new RuntimeException(
              String.format("Failed to start EZMQ subscriber. [ErrorCode=%s]", ezmqErrorCode));
        }
      }
    } else {
      ezmqErrorCode = this.ezmqSubscriber.subscribe();
      if (ezmqErrorCode != EZMQErrorCode.EZMQ_OK) {
        throw new RuntimeException(
            String.format("Failed to start EZMQ subscriber. [ErrorCode=%s]", ezmqErrorCode));
      }
    }
    LOGGER.info("EZMQ Subscriber started. [host={}/port={}]",
        new Object[]{this.host, this.port});
  }

  @Override
  public void run(SourceContext<DataSet> sourceContext) throws Exception {
    if (this.sourceContext == null) {
      this.sourceContext = sourceContext;
    }

    // Do nothing. Messages are sent from callback function.
    this.running = true;
    while (this.running) {
      synchronized (this.waitLock) {
        waitLock.wait(100L);
      }
    }
  }

  @Override
  public void cancel() {
    LOGGER.info("Cancelling EZMQ Source...");
    this.running = false;

    if (this.ezmqSubscriber != null) {
      this.ezmqSubscriber.stop();
      LOGGER.info("EZMQ Subscriber stopped.");
    }

    if (this.ezmqApi != null) {
      LOGGER.info("EZMQ API terminating...");
      this.ezmqApi.terminate(); // Is it safe to terminate here? Singleton.
      LOGGER.info("EZMQ API terminated.");
    }

    // Terminate loop after shutting down EZMQ
    synchronized (this.waitLock) {
      this.waitLock.notify();
    }
    LOGGER.info("EZMQ Source cancelled.");
  }

  @Override
  public void onMessageCB(Event event) {
    onMessageCB(null, event);
  }

  @Override
  public void onMessageCB(String topic, Event event) {
    if (event == null) {
      return;
    }

    JsonParser jsonParser = new JsonParser();
    List<Reading> readings = event.getReadings();
    for (Reading reading : readings) {
      //LOGGER.info(reading.getValue());
      if (this.sourceContext != null) {
        // TODO: This may vary depending on how EZMQ package its values.
        JsonObject obj = jsonParser.parse(reading.getValue().trim()).getAsJsonObject();
        DataSet streamData = DataSet.create(event.getId(), obj.toString());
        streamData.setValue("/topic", topic);
        LOGGER.info("Streaming: {} / {}",
            streamData.getId(), streamData.toString());
        this.sourceContext.collect(streamData);
      }
    }
  }

  public static void main(String[] args) throws Exception {
    EzmqSource source = new EzmqSource("localhost", 5562);
    source.open(null);
    source.run(null);
  }

}
