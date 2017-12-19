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


import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModelParam;
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

        return base;
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

    public HashMap<String, Double> getEMAs() {
        return mPrevEMAs;
    }

    @Override
    DataSet compute(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {
        LOGGER.debug("[Moving Average] Entering calculation");

        List<Number> values;
        Double preEMA = 0.0;
        Double average = 0.0;
        // 1. Get Key from Keys
        for (int index = 0; index < inRecordKeys.size(); index++) {
            // 2. Create cache for ema value of given target key if not exist
            if (!mPrevEMAs.containsKey(inRecordKeys.get(index))) {
                mPrevEMAs.put(inRecordKeys.get(index), 0.0);
            }
            values = (List<Number>) in.getValue(inRecordKeys.get(index), List.class);
            if (values.size() > 0) {
                List<Number> result = new ArrayList<>();
                // 3-1. Iterate if # of given data is more then one record
                for (int loop = 0; loop < values.size(); loop++) {
                    // 3-2. Get the cache which stores the values of give key.
                    // 3. Get the sum value for given key
                    preEMA = mPrevEMAs.get(inRecordKeys.get(index));
                    Double value = Double.parseDouble(values.get(loop).toString());
                    // 2-5. Calculate exponential moving average value
                    average = ((value - preEMA) * mAlpha) + preEMA;
                    // 2-6. Update ema value in queue
                    getEMAs().put(inRecordKeys.get(index), average);
                    // 2-7. Insert moving average value into data set
                    //in.setValue(outRecordKeys.get(index), average);
                    result.add(average);
                }
                if (result.size() > 0) {
                    in.setValue(outRecordKeys.get(index), result);
                }
            }
        }
        LOGGER.debug("[Moving Average] Type : " + getName() + "  Result : " + in.toString());

        return in;
    }
}
