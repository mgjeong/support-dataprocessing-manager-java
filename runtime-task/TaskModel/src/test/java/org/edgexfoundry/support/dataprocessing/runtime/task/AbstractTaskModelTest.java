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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class AbstractTaskModelTest {
    @Test
    public void testCreate() {

        class UserImple extends AbstractTaskModel {

            @Override
            public DataSet calculate(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {
                return DataSet.create();
            }

            @Override
            public TaskType getType() {
                return TaskType.CUSTOM;
            }

            @Override
            public String getName() {
                return "test";
            }

            @Override
            public TaskModelParam getDefaultParam() {
                return new TaskModelParam();
            }

            @Override
            public void setParam(TaskModelParam param) {

            }
        }

        UserImple test = new UserImple();

        test.setInRecordKeys(new ArrayList<>());
        test.setOutRecordKeys(new ArrayList<>());

        Assert.assertNotNull(test.calculate( DataSet.create()));
    }

}
