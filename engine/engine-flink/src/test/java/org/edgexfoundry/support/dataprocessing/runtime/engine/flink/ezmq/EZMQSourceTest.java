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

package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.ezmq;
/*
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.edgexfoundry.domain.core.Event;
import org.edgexfoundry.domain.core.Reading;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;
*/

public class EZMQSourceTest {
    private static final String EZMQ_HOST = "localhost";
    private static final int EZMQ_PORT = 5599;

    //FIXLATER:
    /*
    @Before
    public void initialize() {
        MockitoAnnotations.initMocks(this);
    }

//    @Test
//    public void testEmfErrorCode() throws Exception{
//        EZMQSource sourceA = new EZMQSource(EZMQ_HOST, -1, EZMQMessageType.PROTOBUF_MSG);
//
//        try{
//            sourceA.open(null);
//        } finally{
//            sourceA.cancel();
//        }
//    }

    //FIXLATER: @Test(timeout = 3000L)
    public void testEmfMessageCB() throws Exception {
        EZMQSource source = new EZMQSource(EZMQ_HOST, EZMQ_PORT);
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
            event.addReading(new Reading("1", "1"));
            event.addReading(new Reading("2", "4"));

            source.onMessageCB(event);

            // Try null
            source.onMessageCB(null);
            Thread.sleep(150L);
        } finally {
            source.cancel();
        }
    }

    @Test(timeout = 3000L)
    public void testOpenClose() throws Exception {
        EZMQSource source = new EZMQSource(EZMQ_HOST, EZMQ_PORT);
        try {
            source.open(null);
            Thread.sleep(50L);
        } finally {
            source.cancel();
        }
    }
    */

//    @Test
//    public void testOpenTwice() throws Exception {
//        EZMQSourceThread sourceA = new EZMQSourceThread();
//        EZMQSourceThread sourceB = new EZMQSourceThread();
//        try {
//            sourceA.open();
//            sourceB.open();
//            Thread.sleep(250L);
//        } finally {
//            sourceB.close();
//            sourceA.close();
//        }
//    }

}
