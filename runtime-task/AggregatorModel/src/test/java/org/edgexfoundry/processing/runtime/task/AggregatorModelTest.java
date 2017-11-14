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
package org.edgexfoundry.processing.runtime.task;

import com.google.gson.JsonObject;
import org.edgexfoundry.processing.runtime.task.model.AggregatorModel;
import org.junit.Test;

public class AggregatorModelTest {

    @Test
    public void testStoreFirst() {
        AggregatorModel model = new AggregatorModel();
        TaskModelParam param = model.getDefaultParam();
        param.put("keys", "A B C");
        param.put("aggregateBy", "StoreFirst");
        model.setParam(param);

        //FIXLATER:
        /*
        // A: 0A, B: 0B
        StreamData streamData = new StreamData("K", makeJsonObject("0A", "0B").toString().getBytes());
        StreamData answer = model.calculate(streamData);
        Assert.assertNull(answer);

        // B: 1B
        streamData = new StreamData("L", makeJsonObject("1B").toString().getBytes());
        answer = model.calculate(streamData);
        Assert.assertNull(answer);

        // A: 2A, C: 2C
        streamData = new StreamData("K", makeJsonObject("2A", "2C").toString().getBytes());
        answer = model.calculate(streamData);
        Assert.assertNotNull(answer);

        Assert.assertEquals("0A\t0B\t2C", answer.getStreamData().getValue());
        */
    }

    @Test
    public void testStoreLast() {
        AggregatorModel model = new AggregatorModel();
        TaskModelParam param = model.getDefaultParam();
        param.put("keys", "A B C");
        param.put("aggregateBy", "StoreLast");
        model.setParam(param);

        //FIXLATER:
        /*
        // A: 0A, B: 0B
        StreamData streamData = new StreamData("K", makeJsonObject("0A", "0B").toString().getBytes());
        StreamData answer = model.calculate(streamData);
        Assert.assertNull(answer);

        // B: 1B
        streamData = new StreamData("L", makeJsonObject("1B").toString().getBytes());
        answer = model.calculate(streamData);
        Assert.assertNull(answer);

        // A: 2A, C: 2C
        streamData = new StreamData("K", makeJsonObject("2A", "2C").toString().getBytes());
        answer = model.calculate(streamData);
        Assert.assertNotNull(answer);

        Assert.assertEquals("2A\t1B\t2C", answer.getStreamData().getValue());
        */
    }

    @Test
    public void testStoreById() {
        AggregatorModel model = new AggregatorModel();
        TaskModelParam param = model.getDefaultParam();
        param.put("keys", "A B C");
        param.put("aggregateBy", "Id");
        model.setParam(param);

        //FIXLATER:
        /*
        // A: 0A, B: 0B
        StreamData streamData = new StreamData("0", makeJsonObject("0A", "0B").toString().getBytes());
        StreamData answer = model.calculate(streamData);
        Assert.assertNull(answer);

        // B: 1B
        streamData = new StreamData("1", makeJsonObject("1B").toString().getBytes());
        answer = model.calculate(streamData);
        Assert.assertNull(answer);

        // A: 2A, C: 2C
        streamData = new StreamData("2", makeJsonObject("2A", "2C").toString().getBytes());
        answer = model.calculate(streamData);
        Assert.assertNull(answer);

        // C: 0C
        streamData = new StreamData("0", makeJsonObject("0C").toString().getBytes());
        answer = model.calculate(streamData);
        Assert.assertNotNull(answer);
        Assert.assertEquals("0A\t0B\t0C", answer.getStreamData().getValue());

        // B: 2B
        streamData = new StreamData("2", makeJsonObject("2B").toString().getBytes());
        answer = model.calculate(streamData);
        Assert.assertNotNull(answer);
        Assert.assertEquals("2A\t2B\t2C", answer.getStreamData().getValue());

        // A: 1A, C: 1C
        streamData = new StreamData("1", makeJsonObject("1A", "1C").toString().getBytes());
        answer = model.calculate(streamData);
        Assert.assertNotNull(answer);
        Assert.assertEquals("1A\t1B\t1C", answer.getStreamData().getValue());

        throw new UnsupportedOperationException("Could you implement this for me, please?");
        */
    }

    private JsonObject makeJsonObject(String... arr) {
        JsonObject obj = new JsonObject();
        for (String s : arr) {
            obj.addProperty(String.valueOf(s.charAt(1)), s);
        }
        return obj;
    }
}
