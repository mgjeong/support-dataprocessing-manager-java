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
import java.util.Arrays;
import java.util.List;

public class ErrorModelTest {
    @Test
    public void testGetNameAndType() {
        ErrorModel model = new ErrorModel();
        Assert.assertNotNull(model.getName());
        Assert.assertNotNull(model.getType());
    }

    @Test
    public void testErrorCalculation() {

        double[] records = {
                18.0,
                4.0,
                18.0,
                18.0,
                18.0,
                7.0,
        };

        Double[] predictionsForRecords = {18.0, 18.0, 18.0};

        List<Double> predictionsRecords =
                new ArrayList<Double>(Arrays.asList(predictionsForRecords));
        List<List<Double>> pattern2DDArray = new ArrayList<>();
        pattern2DDArray.add(predictionsRecords);
        ErrorModel model = new ErrorModel();
        TaskModelParam param = new TaskModelParam();//model.getDefaultParam();
        param.put("type", "mse");
        param.put("pattern", pattern2DDArray);
        param.put("default", -1.0);
        param.put("length", predictionsRecords.size());
        param.put("margin", 5);

        List<String> target = new ArrayList<>();
        target.add("/records/TOOL_ID");
        List<String> output = new ArrayList<>();
        output.add("/error_mse");

        model.setParam(param);

        DataSet dataSet = DataSet.create();

        for (int i = 0; i < records.length; i++) {
            DataSet.Record record = DataSet.Record.create();
            record.put("TOOL_ID", records[i]);

            dataSet.addRecord(record);
        }

        dataSet = model.calculate(dataSet, target, output);

        List<Double> mseList = (List<Double>) dataSet.getValue("/error_mse", List.class);
        Assert.assertNotNull(mseList);
        Assert.assertEquals(4, mseList.size());

        for (Double mse : mseList) {
            System.out.println("MSE  Error : " + mse);
        }
        System.out.println(dataSet.toString());

        ErrorModel model1 = new ErrorModel();
        TaskModelParam param1 = new TaskModelParam();//model.getDefaultParam();
        param1.put("type", "rmse");
        param1.put("pattern", pattern2DDArray);
        param1.put("default", -1.0);
        param1.put("length", predictionsRecords.size());
        param1.put("margin", 5);
        model1.setParam(param1);

        dataSet = DataSet.create();

        for (int i = 0; i < records.length; i++) {
            DataSet.Record record = DataSet.Record.create();
            record.put("TOOL_ID", records[i]);

            dataSet.addRecord(record);
        }

        dataSet = model1.calculate(dataSet, target, output);

        mseList = (List<Double>) dataSet.getValue("/error_mse", List.class);
        Assert.assertNotNull(mseList);
        Assert.assertEquals(4, mseList.size());

        for (Double mse : mseList) {
            System.out.println("RMSE  Error : " + mse);
        }
        System.out.println(dataSet.toString());

        ErrorModel model2 = new ErrorModel();
        TaskModelParam param2 = new TaskModelParam();//model.getDefaultParam();
        param2.put("type", "abc");
        param2.put("pattern", pattern2DDArray);
        param2.put("default", -1.0);
        param2.put("length", predictionsRecords.size());
        param2.put("margin", 5);
        model2.setParam(param2);

        dataSet = DataSet.create();

        for (int i = 0; i < records.length; i++) {
            DataSet.Record record = DataSet.Record.create();
            record.put("TOOL_ID", records[i]);

            dataSet.addRecord(record);
        }

        dataSet = model2.calculate(dataSet, target, output);

        mseList = (List<Double>) dataSet.getValue("/error_mse", List.class);
        Assert.assertNotNull(mseList);
        Assert.assertEquals(4, mseList.size());

        for (Double mse : mseList) {
            System.out.println("ABC  Error : " + mse);
        }
        System.out.println(dataSet.toString());

        ErrorModel model3 = new ErrorModel();
        TaskModelParam param3 = new TaskModelParam();//model.getDefaultParam();
        param3.put("type", "abc");
        param3.put("pattern", pattern2DDArray);
        param3.put("default", -1.0);
        param3.put("length", predictionsRecords.size());
        param3.put("margin", 0);
        model3.setParam(param3);

        dataSet = DataSet.create();

        for (int i = 0; i < records.length; i++) {
            DataSet.Record record = DataSet.Record.create();
            record.put("TOOL_ID", records[i]);

            dataSet.addRecord(record);
        }

        dataSet = model3.calculate(dataSet, target, output);

        mseList = (List<Double>) dataSet.getValue("/error_mse", List.class);
        Assert.assertNotNull(mseList);
        Assert.assertEquals(1, mseList.size());

        for (Double mse : mseList) {
            System.out.println("ABC (NO MARGIN) Error : " + mse);
        }
        System.out.println(dataSet.toString());
    }
}
