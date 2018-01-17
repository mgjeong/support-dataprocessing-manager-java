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

import org.edgexfoundry.support.dataprocessing.runtime.task.model.ErrorModel;
import org.junit.Assert;
import org.junit.Test;

public class ErrorModelTest {

    @Test
    public void testConstructor() {


    }

    @Test
    public void testGetProcessingType() {

        ErrorModel model = new ErrorModel();

        Assert.assertEquals(TaskType.ERROR, model.getType());

    }

    @Test
    public void testGetName() {

        ErrorModel model = new ErrorModel();

        Assert.assertEquals("error", model.getName());

    }

    @Test
    public void testSetParam() {

    }

    @Test
    public void testPrediction() {
    }
}
