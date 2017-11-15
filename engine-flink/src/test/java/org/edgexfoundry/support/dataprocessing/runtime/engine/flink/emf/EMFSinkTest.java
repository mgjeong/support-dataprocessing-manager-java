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

package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.emf;
/*
import DataSet;
import org.edgexfoundry.emf.EMFErrorCode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
*/
public class EMFSinkTest {
    private static final String EMF_HOST = "localhost";
    private static final int EMF_PORT = 5599;

    //FIXLATER:
    /*
    @Before
    public void initialize() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(timeout = 3000L)
    public void testInvoke() throws Exception {
        EMFSink sink = new EMFSink(EMF_PORT);
        try {
            sink.open(null);
            sink.onStartCB(EMFErrorCode.EMF_OK);
            sink.onStopCB(EMFErrorCode.EMF_OK);
            sink.onErrorCB(EMFErrorCode.EMF_OK);

            sink.invoke(DataSet.create("{}"));
        } finally {
            sink.close();
        }
    }

    @Test(timeout = 3000L)
    public void testOpenClose() throws Exception {
        EMFSink sink = new EMFSink(EMF_PORT);
        try {
            sink.open(null);
            Thread.sleep(50L);
        } finally {
            sink.close();
        }
    }
    */
}
