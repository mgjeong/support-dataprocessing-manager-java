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

public class AutoregressiveMovingAverage extends MovingAverage {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoregressiveMovingAverage.class);

    private double[] mARParams = null;
    private double[] mMAParams = null;
    private double[] mLagOps = null;
    private double mConstant = 0.0;
    private double mCurError = 0.0;

    public AutoregressiveMovingAverage() {
    }

    @Override
    DataSet compute(DataSet in) {

        List<DataSet.Record> records = in.getRecords();
        LinkedList window = null;
        String targetKey = null;
        String outputKey = null;
        Double average = 0.0;
        // 1. Loop for each records
        for (DataSet.Record record : records) {
            // 2. Loop for each target variables
            for (int index = 0; index < getTarget().size(); index++) {
                // 2-1. Get name of target,output variable and avg value in the queue
                targetKey = getTarget().get(index);
                outputKey = getOutput().get(index);

                window = ((LinkedList) getValues().get(targetKey));
                // 2-2. Update the window & average value
                if (window.size() == getWindowSize() && getWindowSize() > 0) {
                    window.removeFirst();
                }
                // 2-3. Get value from the record
                Object kVal = record.get(targetKey.replace("/", ""));
                Double value = Double.parseDouble(kVal.toString());
                window.addLast(value);
                // 2-4. Calculate error value
                mCurError = mConstant * value;
                // 2-6. Calculate moving average value
                //average = sum / window.size();
                Double[] temp = new Double[window.size()];
                window.toArray(temp);
                average = mCurError + CommonFunction.forceProduct(temp, mARParams)
                        + CommonFunction.forceProduct(temp, mMAParams);
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
        HashMap tMA = (HashMap) params.get("ma");
        HashMap tAR = (HashMap) params.get("ar");
        HashMap tLAGs = (HashMap) params.get("lags");

        if (tMA.containsKey("coefficients")) {
            mMAParams =
                    TaskModelParam.transformToNativeDouble1DArray((ArrayList<Number>) tMA.get("coefficients"));
        }
        if (tAR.containsKey("coefficients")) {
            mARParams =
                    TaskModelParam.transformToNativeDouble1DArray((ArrayList<Number>) tAR.get("coefficients"));
        }
        if (tLAGs.containsKey("coefficients")) {
            mLagOps =
                    TaskModelParam.transformToNativeDouble1DArray((ArrayList<Number>) tLAGs.get("coefficients"));
        }
        if (tMA.containsKey("coefficients")
                && tMA.containsKey("coefficients")
                && tLAGs.containsKey("coefficients")) {
            // SUM of (PSIs * Lags) / SUM of (PHIs * Lags)
            mConstant = CommonFunction.forceProduct(mARParams, mLagOps)
                    / CommonFunction.forceProduct(mMAParams, mLagOps);
        }
    }

    @Override
    public String getName() {
        return "arma";
    }

    @Override
    public TaskModelParam getDefaultParam() {
        Double[] maCoefficientsArray = {0.6, 0.2, 0.1};
        Double[] arCoefficientsArray = {0.5, 0.2, 0.1, 0.05, 0.0};
        Double[] lagOperatorsArray = {0.32, 0.22, 0.11, 0.05, 0.0};

        ArrayList<Double> maCoefficients =
                new ArrayList<Double>(Arrays.asList(maCoefficientsArray));
        ArrayList<Double> arCoefficients =
                new ArrayList<Double>(Arrays.asList(arCoefficientsArray));
        ArrayList<Double> lagOperators =
                new ArrayList<Double>(Arrays.asList(lagOperatorsArray));

        TaskModelParam base = new TaskModelParam();
        HashMap data = new HashMap<String, Object>();
        HashMap ma = new HashMap<String, Object>();
        HashMap ar = new HashMap<String, Object>();
        HashMap lags = new HashMap<String, Object>();

        data.put("data", 3);
        base.put("interval", data);
        ma.put("coefficients", maCoefficients);
        base.put("ma", ma);
        ar.put("coefficients", arCoefficients);
        base.put("ar", ar);
        lags.put("coefficients", lagOperators);
        base.put("lags", lags);
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
