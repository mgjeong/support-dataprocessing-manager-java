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
package org.edgexfoundry.support.dataprocessing.runtime.task;

import org.edgexfoundry.support.dataprocessing.runtime.task.model.QueryModel;
import org.junit.Assert;
import org.junit.Test;

public class QueryModelTest {

    @Test
    public void testGetProcessingType() {

        QueryModel model = new QueryModel();

        Assert.assertEquals(TaskType.QUERY, model.getType());

    }

    @Test
    public void testGetName() {

        TaskModel model1 = new QueryModel();

        Assert.assertEquals("query", model1.getName());

        QueryModel model2 = new QueryModel();

        Assert.assertEquals("query", model2.getName());
    }

    @Test
    public void testGetDefaultParam() {

        TaskModel model1 = new QueryModel();

        Assert.assertNotNull( model1.getDefaultParam());
    }

    @Test
    public void testSetParam() {

        TaskModel model1 = new QueryModel();

        TaskModelParam params = new TaskModelParam();
        params.put("request", "helloworld");
        model1.setParam(params);
    }
}
