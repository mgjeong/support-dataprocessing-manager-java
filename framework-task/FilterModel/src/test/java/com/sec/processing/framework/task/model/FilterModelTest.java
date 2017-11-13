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
package com.sec.processing.framework.task.model;

import com.sec.processing.framework.task.DataSet;
import com.sec.processing.framework.task.TaskModelParam;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FilterModelTest {
    @Test
    public void testGetNameAndType() {
        FilterModel model = new FilterModel();
        Assert.assertNotNull(model.getName());
        Assert.assertNotNull(model.getType());
    }

    @Test
    public void testLarger() {
        Double value = 1.0;

        FilterModel model = new FilterModel();
        TaskModelParam params = new TaskModelParam();
        params.put("filterType", FilterModel.FilterType.GREATER.toString());
        List<Double> thresholds = new ArrayList<>();
        thresholds.add(3.0);
        params.put("threshold", thresholds);
        model.setParam(params);

        DataSet dataSet = DataSet.create();

        List<String> inrecords = new ArrayList<>();
        inrecords.add("/value");

        dataSet.setValue("/value", value);

        dataSet = model.calculate(dataSet, inrecords, null);
        if (null != dataSet) {
            System.out.println("Return : " + dataSet.toString());
        } else {
            System.out.println("stream dropped.");
        }
    }

    @Test
    public void testBetween() {
        FilterModel model = new FilterModel();
        TaskModelParam params = new TaskModelParam();
        params.put("filterType", FilterModel.FilterType.BETWEEN.toString());
        List<Double> thresholds = new ArrayList<>();
        thresholds.add(3.0);
        thresholds.add(5.0);
        params.put("threshold", thresholds);
        model.setParam(params);

        DataSet dataSet = DataSet.create();

        Double value = 4.0;

        List<String> inrecords = new ArrayList<>();
        inrecords.add("/inputKey");

        dataSet.setValue("/inputKey", value);

        dataSet = model.calculate(dataSet, inrecords, null);
        if (null != dataSet) {
            System.out.println("Return : " + dataSet.toString());
        } else {
            System.out.println("stream dropped.");
        }
    }

    @Test
    public void testEqual() {
        FilterModel model = new FilterModel();
        TaskModelParam params = model.getDefaultParam();
        model.setParam(params);

        DataSet dataSet = DataSet.create();

        Double value = 4.0;

        List<String> inrecords = new ArrayList<>();
        inrecords.add("/inputKey");
        dataSet.setValue("/inputKey", value);

        dataSet = model.calculate(dataSet, inrecords, null);
        if (null != dataSet) {
            System.out.println("Return : " + dataSet.toString());
        } else {
            System.out.println("stream dropped.");
        }
    }


    @Test
    public void testLessWithoutKey() {
        FilterModel model = new FilterModel();
        TaskModelParam params = model.getDefaultParam();
        model.setParam(params);

        DataSet dataSet = DataSet.create();

        Double value = 4.0;
        dataSet.setValue("/value", value);

        dataSet = model.calculate(dataSet, null, null);
        if (null != dataSet) {
            System.out.println("Return : " + dataSet.toString());
        } else {
            System.out.println("stream dropped.");
        }
    }

    @Test
    public void testNotBetween() {
        FilterModel model = new FilterModel();
        TaskModelParam params = new TaskModelParam();
        params.put("filterType", FilterModel.FilterType.NOTBETWEEN.toString());
        List<Double> thresholds = new ArrayList<>();
        thresholds.add(3.0);
        thresholds.add(5.0);
        params.put("threshold", thresholds);
        model.setParam(params);

        DataSet dataSet = DataSet.create();

        Double value = 8.0;

        List<String> inrecords = new ArrayList<>();
        inrecords.add("/inputKey");

        dataSet.setValue("/inputKey", value);

        dataSet = model.calculate(dataSet, inrecords, null);
        if (null != dataSet) {
            System.out.println("Return : " + dataSet.toString());
        } else {
            System.out.println("stream dropped.");
        }
    }
}
