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
import com.sec.processing.framework.task.function.CommonFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WeightedMovingAverage extends MovingAverage {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleMovingAverage.class);

    private double[] mMAParams = null;
    private HashMap<String, Double> mSum = null;

    public WeightedMovingAverage() {
        mSum = new HashMap<>();
    }

    public HashMap<String, Double> getSum() {
        return mSum;
    }

    @Override
    DataSet compute(DataSet in) {

        List<DataSet.Record> records = in.getRecords();
        LinkedList window = null;
        String targetKey = null;
        String outputKey = null;
        Double average = 0.0;
        Double sum = 0.0;
        // 1. Loop for each records
        for (DataSet.Record record : records) {
            // 2. Loop for each target variables
            for (int index = 0; index < getTarget().size(); index++) {
                // 2-1. Get name of target,output variable and avg value in the queue
                targetKey = getTarget().get(index); //.replace("/", "");
                outputKey = getOutput().get(index); //.replace("/", "");
                sum = getSum().get(getTarget().get(index)); //.replace("/", ""));
                window = ((LinkedList) getValues().get(targetKey));
                // 2-2. Update the window & average value
                if (window.size() == getWindowSize() && getWindowSize() > 0) {
                    window.removeFirst();
                }
                // 2-3. Get value from the record
                Object kVal = record.get(targetKey.replace("/", ""));
                Double value = Double.parseDouble(kVal.toString());
                window.addLast(value);
                // 2-5. Update sum value in queue
                getSum().put(targetKey, sum);
                // 2-6. Calculate moving average value
                Double[] temp = new Double[window.size()];
                window.toArray(temp);
                average = CommonFunction.forceProduct(temp, mMAParams);
                // 2-7. Insert moving average value into data set
                in.setValue(outputKey, average);
            }
        }
        LOGGER.debug("[Moving Average] Type : " + getName());
        LOGGER.debug("[Moving Average] Result : " + in.toString());

        return in;
    }

    @Override
    public void setParam(TaskModelParam params) {
        super.setParam(params);
        if (params.containsKey("ma")) {
            HashMap tMA = (HashMap) params.get("ma");
            this.mMAParams =
                    TaskModelParam.transformToNativeDouble1DArray((ArrayList<Number>) tMA.get("coefficients"));
        }
    }

    @Override
    public String getName() {
        return "wma";
    }

    @Override
    public TaskModelParam getDefaultParam() {
        Double[] maCoefficientsArray = {0.5, 0.3, 0.2};
        ArrayList<Double> maCofficients =
                new ArrayList<Double>(Arrays.asList(maCoefficientsArray));

        TaskModelParam base = new TaskModelParam();
        HashMap data = new HashMap<String, Object>();
        HashMap ma = new HashMap<String, Object>();
        data.put("data", 3);
        base.put("interval", data);
        ma.put("coefficients", maCofficients);
        base.put("ma", ma);
        List<String> target = new ArrayList<>();
        target.add("/BC");
        target.add("/BL");
        target.add("/BR");
        target.add("/TL");
        target.add("/TR");
        base.put("target", target);
        List<String> output = new ArrayList<>();
        output.add("/MA_BC");
        output.add("/MA_BL");
        output.add("/MA_BR");
        output.add("/MA_TL");
        output.add("/MA_TR");
        base.put("outputKey", output);
        return base;
    }
}
