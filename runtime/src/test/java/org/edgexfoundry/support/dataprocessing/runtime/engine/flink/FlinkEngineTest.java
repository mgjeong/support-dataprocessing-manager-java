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
package org.edgexfoundry.support.dataprocessing.runtime.engine.flink;


import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.JobResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.engine.Engine;
import org.junit.Assert;
import org.junit.Test;

public class FlinkEngineTest {

    @Test
    public void testFlinkEngine() {
        Engine en = new FlinkEngine("localhost", 8081);

        JobResponseFormat format = en.createJob();

        Assert.assertNotNull(format);

        format = en.createJob("abcde");

        Assert.assertNotNull(format);
    }

    @Test
    public void testRun() {
        Engine en = new FlinkEngine("localhost", 8081);

        JobResponseFormat format = en.createJob();

        Assert.assertNotNull(format);

        en.run(format.getJobId());

        en.stop(format.getJobId());

        en.delete(format.getJobId());

    }
}
