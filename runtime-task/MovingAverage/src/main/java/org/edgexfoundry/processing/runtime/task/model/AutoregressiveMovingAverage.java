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

        return base;
    }

    @Override
    DataSet compute(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {
        LOGGER.debug("[Moving Average] Entering calculation");

        List<Number> values;
        Double sum = 0.0;
        Double average = 0.0;

        for (int index = 0; index < inRecordKeys.size(); index++) {
            // 2. Create cache for given target key if not exist
            if (!getValues().containsKey(inRecordKeys.get(index))) {
                getValues().put(inRecordKeys.get(index), new LinkedList<Number>());
            }
            // 3. Get the sum value for given key
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

                        Double value = Double.parseDouble(values.get(loop).toString());
                        mCurError = mConstant * value;
                        // 2-6. Calculate moving average value
                        Number[] temp = new Number[window.size()];
                        window.toArray(temp);
                        average = mCurError + CommonFunction.forceProduct(temp, mARParams)
                                + CommonFunction.forceProduct(temp, mMAParams);
                        //System.out.println("KEY : "+inRecordKeys.get(index)+" SUM : "+sum+" WIN : "+window.size()+" AVG : "+average);
                        // 3-7. Insert moving average value into data set
                        //in.setValue(outRecordKeys.get(index), average);
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
