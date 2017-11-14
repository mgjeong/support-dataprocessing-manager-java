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

import org.edgexfoundry.processing.runtime.task.AbstractTaskModel;
import org.edgexfoundry.processing.runtime.task.DataSet;
import org.edgexfoundry.processing.runtime.task.TaskModelParam;
import org.edgexfoundry.processing.runtime.task.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;


/*
 * Reference : http://teacher.nsrl.rochester.edu/phy_labs/AppendixB/AppendixB.html
 */
public class FilterModel extends AbstractTaskModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterModel.class);


    private static final String PARAM_FILTERTYPE = "filterType";
    private static final String PARAM_THRESHOLD = "threshold";
    private static final String INPUTKEY_VALUE = "/value";

    enum FilterType {
        GREATER,
        EQUALGREATER,
        LESS,
        EQUALLESS,
        EQUAL,
        NOTEQUAL,
        BETWEEN,
        NOTBETWEEN
    }

    private FilterType mFilterType = FilterType.GREATER;
    private List<Double> mThreshold = new ArrayList<>();
    private String mFilterTypeKey = null;
    private String mThresholdKey = null;

    @Override
    /**
     * Override function
     * Get type of this task
     */
    public TaskType getType() {
        return TaskType.FILTER;
    }

    @Override
    /**
     * Override function
     * Get name of this task
     */
    public String getName() {
        return "FilterModel";
    }

    public void setFilterType(String type) {
        mFilterType = FilterType.valueOf(type);
    }

    public void setThresholds(List<Double> threshold) {
        for (double d : threshold) {
            this.mThreshold.add(d);
        }
    }

    public void setThresholdKey(String key) {
        mThresholdKey = key;
    }

    public void setFilterTypeKey(String key) {
        mFilterTypeKey = key;
    }


    @Override
    /**
     * Override function
     * Get default parameter values for the reference
     */
    public TaskModelParam getDefaultParam() {
        TaskModelParam params = new TaskModelParam();
        params.put(PARAM_FILTERTYPE, "EQUAL");

        List<Double> threshold = new ArrayList<>();
        threshold.add(3.0);

        params.put(PARAM_THRESHOLD, threshold);

        return params;
    }

    @Override
    /**
     * Override function
     * Set parameter values which are required for processing this task
     */
    public void setParam(TaskModelParam params) {
        if (params.containsKey(PARAM_FILTERTYPE)) {
            this.setFilterType(params.get(PARAM_FILTERTYPE).toString());
        }
        if (params.containsKey(PARAM_THRESHOLD)) {
            if (params.get(PARAM_THRESHOLD).getClass() == ArrayList.class){
                this.setThresholds((List<Double>) params.get(PARAM_THRESHOLD));
            } else if (params.get(PARAM_THRESHOLD).getClass() == String.class) {
                setThresholdKey((String) params.get(PARAM_THRESHOLD));
            } else {
                throw new InvalidParameterException("PARAM_THRESHOLD is null.");
            }
        }
    }


    public DataSet calculate(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {
        double value = Double.MIN_VALUE;
        if (null == inRecordKeys || inRecordKeys.size() == 0) {
            value = in.getValue(INPUTKEY_VALUE, Double.class);
        } else {
            value = in.getValue(inRecordKeys.get(0), Double.class);
        }

        if (0 == mThreshold.size() && null != mThresholdKey)
        {
            this.setThresholds((List<Double>) in.getValue(mThresholdKey, List.class));
        }

        switch (mFilterType) {
            case GREATER:
                if (value <= mThreshold.get(0)) {
                    return null;
                }
                break;
            case EQUALGREATER:
                if (value < mThreshold.get(0)) {
                    return null;
                }
                break;
            case LESS:
                if (value >= mThreshold.get(0)) {
                    return null;
                }
                break;
            case EQUALLESS:
                if (value > mThreshold.get(0)) {
                    return null;
                }
                break;
            case BETWEEN:
                if (mThreshold.size() < 2){
                    return in;
                }
                if (value < mThreshold.get(0) || mThreshold.get(1) < value) {
                    return null;
                }
                break;
            case NOTBETWEEN:
                if (mThreshold.size() < 2){
                    return in;
                }
                if (mThreshold.get(0) <= value && value <= mThreshold.get(1)) {
                    return null;
                }
                break;
            case EQUAL:
                if (value != mThreshold.get(0)) {
                    return null;
                }
                break;
            case NOTEQUAL:
                if (value == mThreshold.get(0)) {
                    return null;
                }
                break;
            default:
                break;
        }
        return in;
    }
}
