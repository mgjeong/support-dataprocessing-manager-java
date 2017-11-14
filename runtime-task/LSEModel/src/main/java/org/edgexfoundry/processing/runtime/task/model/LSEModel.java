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

import java.util.ArrayList;
import java.util.List;

public class LSEModel extends AbstractTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(LSEModel.class);

    private static final String PARAM_XAXIS = "xAxis";
    private static final String INPUTKEY_VALUE = "/value";
    private static final String OUTPUTKEY_SLOPE = "/slope";
    private static final String OUTPUTKEY_INTERCEPT = "/intercept";

    private List<Double> x = new ArrayList<>();
    private String xKey = null;

    @Override
    public TaskType getType() {
        return TaskType.PREPROCESSING;
    }

    @Override
    public String getName() {
        return "LSEModel";
    }

    private double[] findSlopeNintercept(List<Object> y) {
        int len = y.size();

        //if there is no x-axis parameter, use "1,2,3,.." as index
        if (x.size() != len) {
            x.clear();
            for (int i = 0; i < y.size(); i++) {
                x.add((double) i);
            }
        }

        double sumX = 0.0, sumY = 0.0;
        double meanX = 0.0, meanY = 0.0;
        double xybar = 0.0, xxbar = 0.0;

        for (int index = 0; index < len; index++) {
            sumX += x.get(index);
            sumY += (Double) (y.get(index));
        }

        meanX = (sumX / len);
        meanY = (sumY / len);

        for (int i = 0; i < len; i++) {
            xybar += (x.get(i) - meanX) * ((Double) y.get(i) - meanY);
            xxbar += (x.get(i) - meanX) * (x.get(i) - meanX);
        }

        double[] slopeNintercept = new double[2];
        slopeNintercept[0] = xybar / xxbar; //slope
        slopeNintercept[1] = meanY - (slopeNintercept[0] * meanX); //intercept
        return slopeNintercept;
    }


    @Override
    public void setParam(TaskModelParam params) {
        if (params.containsKey(PARAM_XAXIS)) {
            if (params.get(PARAM_XAXIS).getClass() == ArrayList.class)
            {
                x = (List<Double>) params.get(PARAM_XAXIS);
            } else if (params.get(PARAM_XAXIS).getClass() == String.class) {
                xKey = (String) params.get(PARAM_XAXIS);
            }
        }
    }

    @Override
    public TaskModelParam getDefaultParam() {
        TaskModelParam param = new TaskModelParam();
        List<Double> xAxis = new ArrayList<>();

        double[] data = {2, 4, 6, 8, 10, 12, 14, 16};
        for (double d : data) {
            xAxis.add(d);
        }
        param.put(PARAM_XAXIS, xAxis);
        return param;
    }


    @Override
    public DataSet calculate(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {
        List<Object> valueList = new ArrayList<>();
        if (null == inRecordKeys || inRecordKeys.size() == 0 ) {
            valueList = in.getValue(INPUTKEY_VALUE, List.class);
        } else {
            valueList = in.getValue(inRecordKeys.get(0), List.class);
        }

        if (this.x.size() == 0 && null != xKey)
        {
            this.x = (List<Double>) in.getValue(xKey, List.class);
        }

        double[] slopeNintercept = findSlopeNintercept(valueList);

        if (null == outRecordKeys || outRecordKeys.size() == 0 ) {
            in.setValue(OUTPUTKEY_SLOPE, slopeNintercept[0]);
            in.setValue(OUTPUTKEY_INTERCEPT, slopeNintercept[1]);
        } else {
            in.setValue(outRecordKeys.get(0), slopeNintercept[0]);
            in.setValue(outRecordKeys.get(1), slopeNintercept[1]);
        }

        return in;
    }
}
