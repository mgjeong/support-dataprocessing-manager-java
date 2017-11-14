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

package com.sec.processing.framework.engine.flink.emf;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.edgexfoundry.processing.runtime.task.DataSet;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.edgexfoundry.domain.core.Event;
import org.edgexfoundry.domain.core.Reading;
import org.edgexfoundry.emf.EMFAPI;
import org.edgexfoundry.emf.EMFErrorCode;
import org.edgexfoundry.emf.EMFSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EMFSource extends RichSourceFunction<DataSet> implements EMFSubscriber.EMFSubCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(EMFSource.class);

    private final String host;
    private final int port;

    private EMFAPI emfApi = null;
    private EMFSubscriber emfSubscriber = null;

    private SourceContext<DataSet> sourceContext = null;

    private transient Object waitLock;
    private transient boolean running = false;

    public EMFSource(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        this.waitLock = new Object();

        this.emfApi = EMFAPI.getInstance();
        this.emfApi.initialize();
        LOGGER.info("EMF API initialized.");

        this.emfSubscriber = new EMFSubscriber(this.host, this.port, this);
        EMFErrorCode emfErrorCode = this.emfSubscriber.start();
        if (emfErrorCode != EMFErrorCode.EMF_OK) {
            throw new RuntimeException(
                    String.format("Failed to start EMF subscriber. [ErrorCode=%s]", emfErrorCode));
        }
        emfErrorCode = this.emfSubscriber.subscribe();
        if (emfErrorCode != EMFErrorCode.EMF_OK) {
            throw new RuntimeException(
                    String.format("Failed to start EMF subscriber. [ErrorCode=%s]", emfErrorCode));
        }
        LOGGER.info("EMF Subscriber started. [host={}/port={}]",
                new Object[] {this.host, this.port});
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
        LOGGER.info("Cancelling EMF Source...");
        this.running = false;

        if (this.emfSubscriber != null) {
            this.emfSubscriber.stop();
            LOGGER.info("EMF Subscriber stopped.");
        }

        if (this.emfApi != null) {
            LOGGER.info("EMF API terminating...");
            this.emfApi.terminate(); // Is it safe to terminate here? Singleton.
            LOGGER.info("EMF API terminated.");
        }

        // Terminate loop after shutting down EMF
        synchronized (this.waitLock) {
            this.waitLock.notify();
        }
        LOGGER.info("EMF Source cancelled.");
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
                // TODO: This may vary depending on how EMF package its values.
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
        EMFSource source = new EMFSource("localhost", 5562);
        source.open(null);
        source.run(null);
    }

}
