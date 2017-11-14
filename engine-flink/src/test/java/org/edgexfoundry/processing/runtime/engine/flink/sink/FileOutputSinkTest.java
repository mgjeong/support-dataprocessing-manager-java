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

package org.edgexfoundry.processing.runtime.engine.flink.sink;

import org.edgexfoundry.processing.runtime.task.DataSet;
import org.junit.Test;

import java.io.File;
import java.util.UUID;

public class FileOutputSinkTest {

    @Test
    public void testPayloadStreamData() throws Exception {
        File tempFile = makeTempFile();
        FileOutputSink sink = new FileOutputSink(tempFile.getPath());
        System.out.println("Writing to " + tempFile.getPath());
        try {
            DataSet streamData = DataSet.create("{}");
            for (int i = 0; i < 5; i++) {
                streamData.put("/i" + i, String.valueOf(i * i));
            }
            sink.invoke(streamData);
        } finally {
            sink.close();
            tempFile.deleteOnExit();
        }
    }

    @Test
    public void testSimpleStreamData() throws Exception {
        File tempFile = makeTempFile();
        FileOutputSink sink = new FileOutputSink(tempFile.getPath());
        System.out.println("Writing to " + tempFile.getPath());
        try {
            sink.invoke(DataSet.create("{}"));
        } finally {
            sink.close();
            tempFile.deleteOnExit();
        }
    }

    private File makeTempFile() {
        String property = "java.io.tmpdir";
        String tempDir = System.getProperty(property);
        System.out.println("Temp dir: " + tempDir);
        File temp = new File(tempDir, UUID.randomUUID().toString());
        return temp;
    }
}
