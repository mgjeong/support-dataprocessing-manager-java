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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/*
 * Reference : http://teacher.nsrl.rochester.edu/phy_labs/AppendixB/AppendixB.html
 */
public class HermiteModel extends AbstractTaskModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(HermiteModel.class);

    private String mPointKey;
    private List<String> mTarget;
    private List<String> mResult;
    private double[] mTangents;
    private int mPeriod;
    private int mWindowSize;
    private LinkedList mValues;

    public HermiteModel() {
        mPointKey = new String("");
        mTarget = new ArrayList<>();
        mResult = new ArrayList<>();
        mTangents = null;
        mPeriod = 0;
        mWindowSize = 2;
        mValues = new LinkedList();
    }

    @Override
    /**
     * Override function
     * Get type of this task
     */
    public TaskType getType() {
        return TaskType.TREND; // TODO: Change this
    }

    @Override
    /**
     * Override function
     * Get name of this task
     */
    public String getName() {
        return "Cubic-Hermite-Spline";
    }

    @Override
    /**
     * Override function
     * Get default parameter values for the reference
     */
    public TaskModelParam getDefaultParam() {
        TaskModelParam params = new TaskModelParam();
        params.put("pointKey", "Time");
        List<String> target = new ArrayList<>();
        target.add("BC");
        target.add("BL");
        target.add("BR");
        target.add("TL");
        target.add("TR");
        params.put("target", target);
        params.put("period", 5);
        Double[] tangentArray = {0.6, 0.2};
        List<Double> tangents = Arrays.asList(tangentArray);
        params.put("tangents", tangents);
        List<String> output = new ArrayList<>();
        output.add("Interpolation_BC");
        output.add("Interpolation_BL");
        output.add("Interpolation_BR");
        output.add("Interpolation_TL");
        output.add("Interpolation_TR");
        params.put("outputKey", output);

        return params;
    }

    @Override
    /**
     * Override function
     * Set parameter values which are required for processing this task
     */
    public void setParam(TaskModelParam params) {
        if (params.containsKey("pointKey")) {
            mPointKey = params.get("pointKey").toString();
        }
        if (params.containsKey("target")) {
            mTarget = (List<String>) params.get("target");
        }
        if (params.containsKey("tangents")) {
            mTangents = TaskModelParam.transformToNativeDouble1DArray((List<Number>) params.get("tangents"));
        }
        if (params.containsKey("outputKey")) {
            mResult = (List<String>) params.get("outputKey");
        }
        if (params.containsKey("period")) {
            mPeriod = Integer.parseInt((params.get("period").toString()));
        }
    }

    private double computeFirstBaseFunction(double t) {
        double ret = (2 * Math.pow(t, 3));
        ret -= (3 * Math.pow(t, 2));
        return (ret + 1);
    }

    private double computeSecondBaseFunction(double t) {
        double ret = Math.pow(t, 3);
        ret -= (2 * Math.pow(t, 2));
        return (ret + t);
    }

    private double computeThirdBaseFunction(double t) {
        double ret = ((-2) * Math.pow(t, 3));
        ret += (3 * Math.pow(t, 2));
        return ret;
    }

    private double computeFourthBaseFunction(double t) {
        double ret = Math.pow(t, 3);
        ret -= Math.pow(t, 2);
        return ret;
    }

    private double computeTValue(double prevPoint, double currPoint, double interPoint) {

        return ((interPoint - prevPoint) / (currPoint - prevPoint));
    }

    private DataSet compute(DataSet in) {

        double tVal = 0.0;
        Long interPoint = 0L;
        double interpointVal = 0.0;

        Long prevTime = (Long) ((DataSet.Record) mValues.getFirst())
                .get(mPointKey.replace("/", "")); // ex : "Time"
        Long currTime = (Long) ((DataSet.Record) mValues.getLast())
                .get(mPointKey.replace("/", ""));   // ex : "Time"
        // 1. Find the interpolation points t []
        //    ex: interval = (currTime - prevTime) / mPeriod
        Long interval = ((currTime - prevTime) / Long.valueOf(this.mPeriod));

        // 2. Loop for all the keys
        for (int index = 0; index < this.mTarget.size(); index++) {
            // 2-1. Calculate interpolation p values for each t
            //    ex: loop * mPeriod
            interPoint = prevTime;
            double prevPoint = (double) ((DataSet.Record) mValues.getFirst())
                    .get(mTarget.get(index).replace("/", "")); // ex : "BC"
            double currPoint = (double) ((DataSet.Record) mValues.getLast())
                    .get(mTarget.get(index).replace("/", ""));   // ex : "BC"

            // In order to keep the previous values..
            List<DataSet.Record> interPolation =
                    (List<DataSet.Record>) in.getValue(this.mResult.get(index), List.class); //new ArrayList<>();
            if (interPolation == null) {
                interPolation = new ArrayList<>();
            }
            // Add data of starting point
            DataSet.Record prevRecord = DataSet.Record.create();
            prevRecord.put(this.mPointKey, interPoint);
            prevRecord.put(this.mTarget.get(index), prevPoint);
            interPolation.add(prevRecord);

            for (int index2 = 1; index2 < this.mPeriod; index2++) {
                interPoint = prevTime + (interval * index2);
                tVal = computeTValue(prevTime, currTime, interPoint);
                interpointVal = 0;
                interpointVal += computeFirstBaseFunction(tVal) * prevPoint;
                interpointVal += computeSecondBaseFunction(tVal) * this.mTangents[0];
                interpointVal += computeThirdBaseFunction(tVal) * currPoint;
                interpointVal += computeFourthBaseFunction(tVal) * this.mTangents[1];
                // 2-2. Create a Record to save the result
                //    ex: { "Time" : t[index2] , "BC" : p[index2] }
                DataSet.Record record = DataSet.Record.create();
                record.put(this.mPointKey, interPoint);
                record.put(this.mTarget.get(index), interpointVal);
                // 2-3. Insert into the DataSet with outputKey
                //    ex: { "Interpolation_BC" : [ {"Time" : t[index2] , "BC" : p[index2]},{},{}, ...]
                interPolation.add(record);
            }
            in.setValue(this.mResult.get(index), interPolation);
        }
        return in;
    }

    @Override
    /**
     * Override function which calculate error value
     */
    public DataSet calculate(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {

        List<DataSet.Record> records = in.getRecords();
        for (DataSet.Record record : records) {
            if (mValues.size() == this.mWindowSize && this.mWindowSize > 0) {
                mValues.removeFirst();
            }
            mValues.addLast(record);
            // Note : Since the very first point does not have it previous point
            //        Interpolation will not be executed unless it have two points at least...
            if (this.mValues.size() > 1) {
                in = compute(in);
            }
        }
        return in;
    }
}
