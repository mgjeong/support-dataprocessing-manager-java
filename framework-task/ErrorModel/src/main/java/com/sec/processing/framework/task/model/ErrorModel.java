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

import com.sec.processing.framework.task.AbstractTaskModel;
import com.sec.processing.framework.task.DataSet;
import com.sec.processing.framework.task.TaskModelParam;
import com.sec.processing.framework.task.TaskType;
import com.sec.processing.framework.task.function.ErrorFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * Reference : http://teacher.nsrl.rochester.edu/phy_labs/AppendixB/AppendixB.html
 */
public class ErrorModel extends AbstractTaskModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorModel.class);

    private ErrorFunction.MEASURE mMeasureType = ErrorFunction.MEASURE.MSE;
    private double[][] mPredictions = null;
    /*
     * Note that error value which will be returned from this task cannot be NEGATIVE value
     */
    private double mDefaultError = -1;
    private int mPatternLength = 0;
    private int mMargin = 0;

    @Override
    /**
     * Override function
     * Get type of this task
     * @param mMeasureType (ErrorFunction.MEASURE)
     */
    public TaskType getType() {
        return TaskType.ERROR; // TODO: Change this
    }

    @Override
    /**
     * Override function
     * Get name of this task
     * @param mMeasureType (ErrorFunction.MEASURE)
     */
    public String getName() {
        return "error"; // Change this if necessary
    }

    /*
     * Override function
     * Set type of measure which will be used for error calculation
     *
     * @param mMeasureType (ErrorFunction.MEASURE)
     */
    public void setMeasureType(ErrorFunction.MEASURE mMeasureType) {
        this.mMeasureType = mMeasureType;
    }

    /*
     * Override function
     * Set type of measure which will be used for error calculation
     *
     * @param type (String) "mse", "rmse"
     */
    public void setMeasureType(String type) {
        //System.out.println("IN : "+type + " LEN "+ type.length());
        if (type.equalsIgnoreCase("mse")) {
            this.setMeasureType(ErrorFunction.MEASURE.MSE);
        } else if (type.equalsIgnoreCase("rmse")) {
            this.setMeasureType(ErrorFunction.MEASURE.RMSE);
        } else {
            LOGGER.debug("UNKNOWN TYPE : " + type);
            LOGGER.debug("WILL USE DEFAULT TYPE : MSE");
        }
    }

    @Override
    /**
     * Override function
     * Get default parameter values for the reference
     */
    public TaskModelParam getDefaultParam() {
        TaskModelParam params = new TaskModelParam();

        params.put("type", new String("mse"));
        Double[] pattern = {0.6, 0.2, 0.1};
        List<Double> predictions = new ArrayList(Arrays.asList(pattern));
        List<List<Double>> pattern2DArray = new ArrayList();
        pattern2DArray.add(predictions);
        params.put("pattern", pattern2DArray);
        params.put("default", new String("-1"));
        params.put("length", this.mPatternLength);
        params.put("margin", this.mMargin);

        return params;
    }

    @Override
    /**
     * Override function
     * Set parameter values which are required for processing this task
     */
    public void setParam(TaskModelParam params) {

        if (params.containsKey("type")) {
            this.setMeasureType(params.get("type").toString());
        }
        if (params.containsKey("pattern")) {
            mPredictions = TaskModelParam.transformToNativeDouble2DArray((List<List<Number>>) params.get("pattern"));
        }
        if (params.containsKey("default")) {
            mDefaultError = Double.parseDouble((params.get("default").toString()));
        }
        if (params.containsKey("length")) {
            mPatternLength = Integer.parseInt((params.get("length").toString()));
        }
        if (params.containsKey("margin")) {
            mMargin = Integer.parseInt((params.get("margin").toString()));
        }
    }

    @Override
    /**
     * Override function which calculate error value
     */
    public DataSet calculate(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {

        List<Double> errors = new ArrayList<>();
        // 0. Extract target attributes and its values
        List<List<Number>> vectors = new ArrayList<>();
        for (int index = 0; index < inRecordKeys.size(); index++) {
            List<Number> values = in.getValue(inRecordKeys.get(index), List.class);
            vectors.add(values);
        }
        // 1. Check all values are extracted..
        if (vectors.size() == inRecordKeys.size()) {
            // 2. Loop for all the  target attributes
            for (int attr = 0; attr < vectors.size(); attr++) {
                errors.clear();
                if (vectors.get(attr).size() == this.mPatternLength) {
                    // If the size of the array is expected size then just pass..
                    // Note that the mPatternLength is equal to size of this.mPredictions
                    errors.add(0.0);
                } else if ((vectors.get(attr).size() > this.mPatternLength)
                        && (vectors.get(attr).size() <= (this.mPatternLength + this.mMargin))) {
                    // If the size of the array lies in between expected & marginal range
                    int index = 0;
                    // Size will be 0 if the received records size is equal to expected size..
                    // Iterate error calculation for each sliding windows
                    while ((index + this.mPatternLength) <= vectors.get(attr).size()) {

                        List<Number> temp = vectors.get(attr).subList(index, index + this.mPatternLength);
                        //System.out.println(" Records : "+ temp.toString());
                        double[] toolID = new double[temp.size()];
                        for (int iter = 0; iter < temp.size(); iter++) {

                            //System.out.println("TOODL ID : "+temp.get(iter).getValue("TOOL_ID").toString());
                            toolID[iter] = temp.get(iter).doubleValue();
                        }
                        errors.add(Double.valueOf(ErrorFunction.calculate(toolID, this.mPredictions[attr], this.mMeasureType)));

                        index++;
                    }
                } else {
                    // If the size of the array is less the marginal range
                    LOGGER.error("SIZE OF THE RECEIVED DATA IS LESS THAN THE EXPECTED- RECEIVED("
                            + vectors.get(attr).size() + "), EXPECTED(" + this.mPatternLength + ")");
                    errors.add(mDefaultError);
                }

                if (outRecordKeys.size() == inRecordKeys.size()) {
                    in.setValue(outRecordKeys.get(attr), errors);
                } else {
                    LOGGER.debug("[ERR MODEL] Generating output key :", outRecordKeys.get(0) + "_" + attr);
                    in.setValue(outRecordKeys.get(0) + "_" + attr, errors);
                }
            }
        } else {
            LOGGER.error("[ERR MODEL] Extracting value from the give failure");
        }

        return in;
    }
}
