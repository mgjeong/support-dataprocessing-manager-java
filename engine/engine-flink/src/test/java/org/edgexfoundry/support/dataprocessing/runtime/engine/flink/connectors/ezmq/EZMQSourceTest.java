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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.edgexfoundry.domain.core.Event;
import org.edgexfoundry.domain.core.Reading;
import org.edgexfoundry.ezmq.EZMQErrorCode;
import org.edgexfoundry.ezmq.EZMQSubscriber;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EzmqSource.class, EZMQSubscriber.class})
public class EZMQSourceTest {

  private static final String EZMQ_HOST = "localhost";
  private static final int EZMQ_PORT = 5599;
  private static final String EZMQ_TOPIC = "test_topic";

  @Before
  public void initialize() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testEzmqErrorCode() throws Exception {
    EzmqSource sourceA = new EzmqSource(EZMQ_HOST, EZMQ_PORT);

    try {
      sourceA.open(null);
    } finally {
      sourceA.cancel();
    }
  }

  @Test
  public void testEzmqMessageCB() throws Exception {
    EzmqSource source = new EzmqSource(EZMQ_HOST, EZMQ_PORT);
    Thread temp = null;
    try {
      source.open(null);
      SourceFunction.SourceContext sourceContext = mock(SourceFunction.SourceContext.class);

      // Run this on a separate thread (blocking)
      temp = new Thread(() -> {
        try {
          source.run(sourceContext);
        } catch (Exception e) {
          e.printStackTrace();
          Assert.fail(e.getMessage());
        }
      });
      temp.start();

      // Try event
      Event event = new Event("Sample device");
      event.addReading(new Reading("name", "{\"name\":\"value\"}"));
      //event.addReading(new Reading("2", "4"));

      source.onMessageCB(event);
      source.onMessageCB("topic", event);

      // Try null
      source.onMessageCB(null);
      Thread.sleep(150L);
    } finally {
      source.cancel();
    }
  }

  @Test
  public void testOpenClose() throws Exception {
    EzmqSource source = new EzmqSource(EZMQ_HOST, EZMQ_PORT);
    try {
      source.open(null);
      Thread.sleep(50L);
    } finally {
      source.cancel();
    }
  }

  @Test(expected = RuntimeException.class)
  public void testOpenWithSubscribeError() throws Exception {
    EZMQSubscriber subscriber = Mockito.mock(EZMQSubscriber.class);
    Mockito.when(subscriber.subscribe()).thenReturn(EZMQErrorCode.EZMQ_ERROR);
    Mockito.when(subscriber.start()).thenReturn(EZMQErrorCode.EZMQ_OK);
    PowerMockito.mockStatic(EZMQSubscriber.class);
    PowerMockito.whenNew(EZMQSubscriber.class).withArguments(anyString(), anyInt(), any())
        .thenReturn(subscriber);
    EzmqSource source = new EzmqSource(EZMQ_HOST, EZMQ_PORT);
    try {
      source.open(null);
      Thread.sleep(50L);
    } finally {
      source.cancel();
    }
  }

  @Test(expected = RuntimeException.class)
  public void testOpenWithSubscribeErrorTopics() throws Exception {
    EZMQSubscriber subscriber = Mockito.mock(EZMQSubscriber.class);
    Mockito.when(subscriber.subscribe()).thenReturn(EZMQErrorCode.EZMQ_ERROR);
    Mockito.when(subscriber.start()).thenReturn(EZMQErrorCode.EZMQ_OK);
    PowerMockito.mockStatic(EZMQSubscriber.class);
    PowerMockito.whenNew(EZMQSubscriber.class).withArguments(anyString(), anyInt(), any())
        .thenReturn(subscriber);
    EzmqSource source = new EzmqSource(EZMQ_HOST, EZMQ_PORT, EZMQ_TOPIC);
    try {
      source.open(null);
      Thread.sleep(50L);
    } finally {
      source.cancel();
    }
  }

  @Test(expected = RuntimeException.class)
  public void testOpenWithStartError() throws Exception {
    EZMQSubscriber subscriber = Mockito.mock(EZMQSubscriber.class);
    Mockito.when(subscriber.subscribe()).thenReturn(EZMQErrorCode.EZMQ_ERROR);
    Mockito.when(subscriber.start()).thenReturn(EZMQErrorCode.EZMQ_ERROR);
    PowerMockito.mockStatic(EZMQSubscriber.class);
    PowerMockito.whenNew(EZMQSubscriber.class).withArguments(anyString(), anyInt(), any())
        .thenReturn(subscriber);
    EzmqSource source = new EzmqSource(EZMQ_HOST, EZMQ_PORT);
    try {
      source.open(null);
      Thread.sleep(50L);
    } finally {
      source.cancel();
    }
  }


}
