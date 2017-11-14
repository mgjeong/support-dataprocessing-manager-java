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

import org.edgexfoundry.processing.runtime.task.model.LSEModel;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LSEModelTest {

    @Test
    public void testGetProcessingType() {

        LSEModel model = new LSEModel();
        Assert.assertEquals(TaskType.PREPROCESSING, model.getType());
    }

    @Test
    public void testGetName() {

        LSEModel model = new LSEModel();
        Assert.assertEquals("LSEModel", model.getName());
    }


    @Test
    public void test() {
        LSEModel model = new LSEModel();
        TaskModelParam params = model.getDefaultParam();

        model.setParam(params);

        double[] ob = {1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007};

        List<Double> observation = new ArrayList<>();

        for (double d : ob) {
            observation.add(d);
        }

        DataSet dataSet = DataSet.create();
        List<String> inrecords = new ArrayList<>();
        inrecords.add("/value");

        dataSet.setValue("/value", observation);


        List<String> outrecords = new ArrayList<>();
        outrecords.add("/slope");
        outrecords.add("/intercept");

        dataSet = model.calculate(dataSet, inrecords, outrecords);

        if (null != dataSet) {
            System.out.println("Return : " + dataSet.toString());
        } else {
            System.out.println("Stream is null");
        }
    }

    @Test
    public void testWithoutKey() {
        LSEModel model = new LSEModel();
        TaskModelParam params = new TaskModelParam();

        model.setParam(params);

        double[] ob = {1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007};

        List<Double> observation = new ArrayList<>();

        for (double d : ob) {
            observation.add(d);
        }

        DataSet dataSet = DataSet.create();
        dataSet.setValue("/value", observation);

        dataSet = model.calculate(dataSet, null, null);

        if (null != dataSet) {
            System.out.println("Return : " + dataSet.toString());
        } else {
            System.out.println("Stream is null");
        }
    }


    @Test
    public void testWithXkey() {
        LSEModel model = new LSEModel();
        TaskModelParam params = new TaskModelParam();

        double[] ob = {1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007};
        double[] k = {5, 10, 15, 30, 45, 100, 105, 150};

        List<Double> observation = new ArrayList<>();
        List<Double> kList = new ArrayList<>();

        for (double d : ob) {
            observation.add(d);
        }

        for (double d : k) {
            kList.add(d);
        }

        DataSet dataSet = DataSet.create();
        dataSet.setValue("/value", observation);
        dataSet.setValue("/xAxis", kList);

        params.put("xAxis", "/xAxis");
        model.setParam(params);

        dataSet = model.calculate(dataSet, null, null);

        if (null != dataSet) {
            System.out.println("Return : " + dataSet.toString());
        } else {
            System.out.println("Stream is null");
        }
    }
}
