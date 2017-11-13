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


import com.sec.processing.framework.task.DataSet;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.edgexfoundry.domain.core.Event;
import org.edgexfoundry.domain.core.Reading;
import org.edgexfoundry.emf.EMFAPI;
import org.edgexfoundry.emf.EMFCallback;
import org.edgexfoundry.emf.EMFErrorCode;
import org.edgexfoundry.emf.EMFPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class EMFSink extends RichSinkFunction<DataSet> implements EMFCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(EMFSink.class);

    private final int port;

    private EMFAPI emfApi = null;
    private EMFPublisher emfPublisher = null;

    public EMFSink(int port) {
        this.port = port;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        this.emfApi = EMFAPI.getInstance();
        this.emfApi.initialize();
        LOGGER.info("EMF API initialized.");

        this.emfPublisher = new EMFPublisher(this.port, this);
        EMFErrorCode emfErrorCode = this.emfPublisher.start();
        if (emfErrorCode != EMFErrorCode.EMF_OK) {
            throw new RuntimeException(
                    String.format("Failed to start EMFPublisher. [ErrorCode=%s]", emfErrorCode));
        }
    }

    @Override
    public void invoke(DataSet dataSet) throws Exception {
        if (this.emfPublisher != null) {
            Event event = makeEvent(dataSet);
            this.emfPublisher.publish(event);
        }
    }

    @Override
    public void close() throws Exception {
        super.close();

        if (this.emfPublisher != null) {
            this.emfPublisher.stop();
            LOGGER.info("EMF Publisher stopped.");
        }

        if (this.emfApi != null) {
            this.emfApi.terminate();
            LOGGER.info("EMF API terminated.");
        }
    }

    private Event makeEvent(DataSet dataSet) {
        //TODO: Convert from stream data to event accordingly.
        List<Reading> readings = new ArrayList();
        Reading reading = new Reading();
        reading.setName("DPFW");
        reading.setValue(dataSet.getStreamedRecord().toString());
        reading.setId("DPFW-01");
        reading.setOrigin(new Timestamp(System.currentTimeMillis()).getTime());
        reading.setPushed(reading.getOrigin());

        readings.add(reading);
        Event event = new Event("EMFSink", readings);
        event.setCreated(0);
        event.setModified(0);
        event.setId("DPFW-01");
        event.markPushed(new Timestamp(System.currentTimeMillis()).getTime());
        event.setOrigin(event.getPushed());

        return event;
    }

    @Override
    public void onStartCB(EMFErrorCode emfErrorCode) {
        LOGGER.info("EMF publisher started. [ErrorCode={}]", emfErrorCode);
    }

    @Override
    public void onStopCB(EMFErrorCode emfErrorCode) {
        LOGGER.info("EMF publisher stopped. [ErrorCode={}]", emfErrorCode);
    }

    @Override
    public void onErrorCB(EMFErrorCode emfErrorCode) {
        LOGGER.info("EMF publisher error occurred. [ErrorCode={}]", emfErrorCode);
    }
}
