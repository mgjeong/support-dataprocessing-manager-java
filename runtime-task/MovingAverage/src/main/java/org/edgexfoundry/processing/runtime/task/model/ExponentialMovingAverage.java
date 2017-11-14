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
package org.edgexfoundry.processing.runtime.task.model;

import org.edgexfoundry.processing.runtime.task.DataSet;
import org.edgexfoundry.processing.runtime.task.TaskModelParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExponentialMovingAverage extends MovingAverage {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExponentialMovingAverage.class);

    private double mAlpha = 0.0; // Smoothing Factor
    private HashMap<String, Double> mPrevEMAs = null;

    public ExponentialMovingAverage() {
        mPrevEMAs = new HashMap<>();
    }

    public HashMap<String, Double> getEMAs() {
        return mPrevEMAs;
    }

    @Override
    DataSet compute(DataSet in) {

        List<DataSet.Record> records = in.getRecords();

        String targetKey = null;
        String outputKey = null;
        Double average = 0.0;
        Double preEMA = 0.0;
        // 1. Loop for each records
        for (DataSet.Record record : records) {
            // 2. Loop for each target variables
            for (int index = 0; index < getTarget().size(); index++) {
                // 2-1. Get name of target,output variable and avg value in the queue
                targetKey = getTarget().get(index); //.replace("/", "");
                outputKey = getOutput().get(index); //.replace("/", "");
                preEMA = getEMAs().get(getTarget().get(index)); //.replace("/", ""));
                // 2-3. Get value from the record
                Object kVal = record.get(targetKey.replace("/", ""));
                Double value = Double.parseDouble(kVal.toString());
                // 2-5. Calculate exponential moving average value
                average = ((value - preEMA) * mAlpha) + preEMA;
                // 2-6. Update ema value in queue
                getEMAs().put(targetKey, average);
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
        Double paramAlpha = (Double) params.get("alpha");

        if (paramAlpha != null) {
            mAlpha = paramAlpha.doubleValue();
        } else {
            mAlpha = (2.0 / (1 + getWindowSize()));
        }
        for (int index = 0; index < getTarget().size(); index++) {
            mPrevEMAs.put(getTarget().get(index), 0.0);
        }
    }

    @Override
    public String getName() {
        return "ema";
    }

    @Override
    public TaskModelParam getDefaultParam() {

        TaskModelParam base = new TaskModelParam();
        HashMap data = new HashMap<String, Object>();
        data.put("data", 3);
        base.put("interval", data);
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
