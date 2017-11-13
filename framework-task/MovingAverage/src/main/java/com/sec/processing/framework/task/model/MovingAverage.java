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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public abstract class MovingAverage extends AbstractTaskModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(MovingAverage.class);

    private int windowType = -1; // Data(0), Time(1);
    private int mWindowSize = 2;

    private HashMap<String, LinkedList> mValues = null;
    private List<String> mTarget = null;
    private List<String> mResult = null;

    public MovingAverage() {
        mValues = new HashMap<>();
        mTarget = new ArrayList<>();
        mResult = new ArrayList<>();
    }

    public void setWindowType(int windowType) {
        this.windowType = windowType;
    }

    public long getWindowSize() {
        return mWindowSize;
    }

    public void setWindowSize(int mWindowSize) {
        this.mWindowSize = mWindowSize;
    }

    public HashMap getValues() {
        return mValues;
    }

    public List<String> getTarget() {
        return mTarget;
    }

    public List<String> getOutput() {
        return mResult;
    }

    @Override
    public TaskType getType() {
        return TaskType.TREND;
    }

    @Override
    public DataSet calculate(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {

        in = compute(in);

        return in;
    }

    @Override
    public void setParam(TaskModelParam params) {

        if (params.containsKey("target")) {
            mTarget = (List<String>) params.get("target");

            for (int index = 0; index < getTarget().size(); index++) {
                mValues.put(getTarget().get(index), new LinkedList<Double>());
            }
        }
        if (params.containsKey("outputKey")) {
            mResult = (List<String>) params.get("outputKey");
        }
        if (params.containsKey("interval")) {
            HashMap tInterval = (HashMap) params.get("interval");
            if (tInterval.containsKey("data")) {
                Integer dataSize = (Integer) tInterval.get("data");
                if (dataSize != null) {
                    setWindowType(0);
                    setWindowSize(dataSize.intValue());
                }
            }
            if (tInterval.containsKey("time")) {
                Integer timeSize = (Integer) tInterval.get("time");
                if (timeSize != null) {
                    setWindowType(1);
                    setWindowSize(timeSize.intValue());
                }
            }
        }
    }

    abstract DataSet compute(DataSet in);
}
