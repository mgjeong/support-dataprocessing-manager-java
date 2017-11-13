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
import java.util.LinkedList;
import java.util.List;

/*
 * Reference : https://en.wikipedia.org/wiki/Linear_interpolation
 */
public class LinearInterpolationModel extends AbstractTaskModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(LinearInterpolationModel.class);

    private String mPointKey;
    private List<String> mTarget;
    private List<String> mResult;
    private int mPeriod = 0;
    private int mWindowSize = 2;
    private LinkedList mValues;

    public LinearInterpolationModel() {
        mPointKey = new String("");
        mTarget = new ArrayList<>();
        mResult = new ArrayList<>();
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
        return "Linear-interpolation";
    }

    @Override
    /**
     * Override function
     * Get default parameter values for the reference
     */
    public TaskModelParam getDefaultParam() {
        TaskModelParam params = new TaskModelParam();
        params.put("pointKey", "/Time");
        List<String> target = new ArrayList<>();
        target.add("/BC");
        target.add("/BL");
        target.add("/BR");
        target.add("/TL");
        target.add("/TR");
        params.put("target", target);
        params.put("period", 30);

        List<String> output = new ArrayList<>();
        output.add("/Interpolation_BC");
        output.add("/Interpolation_BL");
        output.add("/Interpolation_BR");
        output.add("/Interpolation_TL");
        output.add("/Interpolation_TR");
        params.put("outputKey", output);

        TaskModelParam interval = new TaskModelParam();
        interval.put("data", 2);
        params.put("interval", interval);
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
        if (params.containsKey("outputKey")) {
            mResult = (List<String>) params.get("outputKey");
        }
        if (params.containsKey("period")) {
            mPeriod = Integer.parseInt((params.get("period").toString()));
        }
        if (params.containsKey("interval")) {
            TaskModelParam tInterval = (TaskModelParam) params.get("interval");
            Integer dataSize = (Integer) tInterval.get("data");
            if (dataSize != null) {
                this.mWindowSize = dataSize.intValue();
            } 
        }        
    }

    private double computeFirstBaseFunction(double interT, double prevT, double currT) {
        double ret = (interT / prevT) / (currT - prevT);
        return (1 - ret);
    }

    private double computeSecondBaseFunction(double interT, double prevT, double currT) {
        double ret = (interT / prevT) / (currT - prevT);
        return (ret);
    }

    private double computeTValue(double prevPoint, double currPoint, double interPoint) {

        return ((interPoint - prevPoint) / (currPoint - prevPoint));
    }

    private DataSet compute(DataSet in) {

        double tVal = 0.0;
        Long interPoint = 0L;
        double interpointVal = 0.0;

        Integer prevTime = (Integer) ((DataSet.Record) mValues.getFirst())
                .get(mPointKey.replace("/", "")); // ex : "Time"
        Integer currTime = (Integer) ((DataSet.Record) mValues.getLast())
                .get(mPointKey.replace("/", ""));   // ex : "Time"
        // 1. Find the interpolation points t []
        //    ex: interval = (currTime - prevTime) / mPeriod
        //Long interval = (((Integer)(currTime - prevTime)).longValue() / Long.valueOf(this.mPeriod));
        Long interval = ((currTime - prevTime) / Long.valueOf(this.mPeriod));

        // 2. Loop for all the keys
        for (int index = 0; index < this.mTarget.size(); index++) {
            // 2-1. Calculate interpolation p values for each t
            //    ex: loop * mPeriod
            interPoint = prevTime.longValue();

            Object prevP = ((DataSet.Record) mValues.getFirst())
                    .get(mTarget.get(index).replace("/", "")); // ex : "BC"
            Object currP = ((DataSet.Record) mValues.getLast())
                    .get(mTarget.get(index).replace("/", "")); // ex : "BC"

            double prevPoint = 0.0;
            double currPoint = 0.0;
            if (prevP instanceof Integer) {
                prevPoint = ((Integer) prevP).doubleValue();
            } else if (prevP instanceof Long) {
                prevPoint = ((Long) prevP).doubleValue();
            }

            if (currP instanceof Integer) {
                currPoint = ((Integer) currP).doubleValue();
            } else if (currP instanceof Long) {
                currPoint = ((Long) currP).doubleValue();
            }

            // In order to keep the previous values..
            List<DataSet.Record> interPolation =
                    (List<DataSet.Record>) in.getValue(this.mResult.get(index), List.class); //new ArrayList<>();
            if (interPolation == null) {
                interPolation = new ArrayList<>();
            }
            // Add data of starting point
            DataSet.Record prevRecord = DataSet.Record.create();
            prevRecord.put(this.mPointKey.replace("/", ""), interPoint);
            prevRecord.put(this.mTarget.get(index).replace("/", ""), prevPoint);
            interPolation.add(prevRecord);

            for (int index2 = 1; index2 < this.mPeriod; index2++) {

                interPoint = prevTime + (interval * index2);
                tVal = computeTValue(prevTime, currTime, interPoint);
                interpointVal = 0;
                interpointVal += computeFirstBaseFunction(tVal, prevTime, currTime) * prevPoint;
                interpointVal += computeSecondBaseFunction(tVal, prevTime, currTime) * currPoint;

                // 2-2. Create a Record to save the result
                //    ex: { "Time" : t[index2] , "BC" : p[index2] }
                DataSet.Record record = DataSet.Record.create();
                record.put(this.mPointKey.replace("/", ""), interPoint);
                record.put(this.mTarget.get(index).replace("/", ""), interpointVal);
                // 2-3. Insert into the DataSet with outputKey
                //    ex: { "Interpolation_BC" : [ {"Time" : t[index2] , "BC" : p[index2]},{},{}, ...]
                interPolation.add(record);
            }
            in.setValue(this.mResult.get(index), interPolation);
            LOGGER.debug(in.toString());
        }

        return in;
    }

    @Override
    /**
     * Override function which calculate interpolation value(s)
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
            } else {
                in = null;
                LOGGER.debug("Need one point more to process");
            }
        }
        return in;
    }
}
