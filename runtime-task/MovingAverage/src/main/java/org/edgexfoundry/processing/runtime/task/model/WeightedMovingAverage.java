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
import org.edgexfoundry.support.dataprocessing.runtime.task.function.CommonFunction;
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

        return base;
    }

    public HashMap<String, Double> getSum() {
        return mSum;
    }

    @Override
    DataSet compute(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {
        LOGGER.debug("[Moving Average] Entering calculation");

        List<Number> values;
        Double sum = 0.0;
        Double average = 0.0;
        for (int index = 0; index < inRecordKeys.size(); index++) {
            // 2. Create cache for given target key if not exist
            if (!mSum.containsKey(inRecordKeys.get(index))) {
                mSum.put(inRecordKeys.get(index), 0.0);
                getValues().put(inRecordKeys.get(index), new LinkedList<Number>());
            }
            // 3. Get the sum value for given key
            sum = mSum.get(inRecordKeys.get(index));
            values = (List<Number>) in.getValue(inRecordKeys.get(index), List.class);
            if (values.size() > 0) {
                List<Number> result = new ArrayList<>();
                // 3-1. Iterate if # of given data is more then one record
                for (int loop = 0; loop < values.size(); loop++) {
                    // 3-2. Get the cache which stores the values of give key.
                    LinkedList window = ((LinkedList) getValues().get(inRecordKeys.get(index)));
                    if (window != null) {
                        // 3-3. If cache is not full
                        if (window.size() == getWindowSize() && getWindowSize() > 0) {
                            sum -= (Double.parseDouble(window.getFirst().toString()));
                            // Pull out the oldest value
                            window.removeFirst();
                        }
                        // 3-4. Push the new value
                        window.addLast(values.get(loop));
                        sum += Double.parseDouble(values.get(loop).toString());
                        // 3-5. Update sum value in cache
                        getSum().put(inRecordKeys.get(index), sum);
                        // 3-6. Calculate moving average value
                        Number[] temp = new Number[window.size()];
                        window.toArray(temp);
                        average = CommonFunction.forceProduct(temp, mMAParams);
                        // 3-7. Insert moving average value into data set
                        result.add(average);
                    } else {
                        LOGGER.error("[Moving Average] Value of [" + inRecordKeys.get(index) + " Not exist~!!!");
                    }
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
