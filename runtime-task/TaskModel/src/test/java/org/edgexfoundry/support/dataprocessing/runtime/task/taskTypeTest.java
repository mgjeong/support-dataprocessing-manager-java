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

import org.junit.Assert;
import org.junit.Test;

public class taskTypeTest {

    @Test
    public void toIntTest() {

        Assert.assertEquals(TaskType.CUSTOM.toInt(), 0);

        Assert.assertEquals(TaskType.CLASSIFICATION.toInt(), 1);

        Assert.assertEquals(TaskType.CLUSTERING.toInt(), 2);

        Assert.assertEquals(TaskType.PATTERN.toInt(), 3);

        Assert.assertEquals(TaskType.TREND.toInt(), 4);

        Assert.assertEquals(TaskType.PREPROCESSING.toInt(), 5);

        Assert.assertEquals(TaskType.REGRESSION.toInt(), 6);

        Assert.assertEquals(TaskType.FILTER.toInt(), 7);

        Assert.assertEquals(TaskType.ERROR.toInt(), 8);

        Assert.assertEquals(TaskType.OUTLIER.toInt(), 9);

        Assert.assertEquals(TaskType.INVALID.toInt(), 10);

    }

    @Test
    public void toStringTest() {

        Assert.assertEquals(TaskType.CUSTOM.toString(), "CUSTOM");

        Assert.assertEquals(TaskType.CLASSIFICATION.toString(), "CLASSIFICATION");

        Assert.assertEquals(TaskType.CLUSTERING.toString(), "CLUSTERING");

        Assert.assertEquals(TaskType.PATTERN.toString(), "PATTERN");

        Assert.assertEquals(TaskType.TREND.toString(), "TREND");

        Assert.assertEquals(TaskType.PREPROCESSING.toString(), "PREPROCESSING");

        Assert.assertEquals(TaskType.REGRESSION.toString(), "REGRESSION");

        Assert.assertEquals(TaskType.FILTER.toString(), "FILTER");

        Assert.assertEquals(TaskType.ERROR.toString(), "ERROR");

        Assert.assertEquals(TaskType.OUTLIER.toString(), "OUTLIER");

        Assert.assertEquals(TaskType.INVALID.toString(), "INVALID");

    }

    @Test
    public void getTypeByIntTest() {

        Assert.assertEquals(TaskType.getType(0), TaskType.CUSTOM);

        Assert.assertEquals(TaskType.getType(1), TaskType.CLASSIFICATION);

        Assert.assertEquals(TaskType.getType(2), TaskType.CLUSTERING);

        Assert.assertEquals(TaskType.getType(3), TaskType.PATTERN);

        Assert.assertEquals(TaskType.getType(4), TaskType.TREND);

        Assert.assertEquals(TaskType.getType(5), TaskType.PREPROCESSING);

        Assert.assertEquals(TaskType.getType(6), TaskType.REGRESSION);

        Assert.assertEquals(TaskType.getType(7), TaskType.FILTER);

        Assert.assertEquals(TaskType.getType(8), TaskType.ERROR);

        Assert.assertEquals(TaskType.getType(9), TaskType.OUTLIER);

        Assert.assertEquals(TaskType.getType(10), TaskType.INVALID);

    }

    @Test
    public void getTypeByTypeTest() {

        Assert.assertEquals(TaskType.getType("REGRESSION"), TaskType.REGRESSION);

        Assert.assertEquals(TaskType.getType("CLASSIFICATION"), TaskType.CLASSIFICATION);

        Assert.assertEquals(TaskType.getType("CLUSTERING"), TaskType.CLUSTERING);

        Assert.assertEquals(TaskType.getType("PATTERN"), TaskType.PATTERN);

        Assert.assertEquals(TaskType.getType("TREND"), TaskType.TREND);

        Assert.assertEquals(TaskType.getType("PREPROCESSING"), TaskType.PREPROCESSING);

        Assert.assertEquals(TaskType.getType("CUSTOM"), TaskType.CUSTOM);

        Assert.assertEquals(TaskType.getType("OUTLIER"), TaskType.OUTLIER);

        Assert.assertEquals(TaskType.getType("INVALID"), TaskType.INVALID);

        Assert.assertEquals(TaskType.getType("Hello"), null);

    }
}
