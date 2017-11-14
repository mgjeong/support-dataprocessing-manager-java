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
package org.edgexfoundry.processing.runtime.task;

public enum TaskType {
    CUSTOM(0),  CLASSIFICATION(1), CLUSTERING(2), PATTERN(3), TREND(4), PREPROCESSING(5), REGRESSION(6), FILTER(7), ERROR(8),
    OUTLIER(9), INVALID(10);

    private int mType;

    TaskType(int type) {
        mType = type;
    }

    public int toInt() {
        return mType;
    }

    @Override
    public String toString() {
        return this.name().toUpperCase();
    }

    public static TaskType getType(int type) {
        if (type == REGRESSION.toInt()) {
            return REGRESSION;
        } else if (type == CLASSIFICATION.toInt()) {
            return CLASSIFICATION;
        } else if (type == CLUSTERING.toInt()) {
            return CLUSTERING;
        } else if (type == PATTERN.toInt()) {
            return PATTERN;
        } else if (type == TREND.toInt()) {
            return TREND;
        } else if (type == PREPROCESSING.toInt()) {
            return PREPROCESSING;
        } else if (type == FILTER.toInt()) {
            return FILTER;
        } else if (type == ERROR.toInt()) {
            return ERROR;
        } else if (type == OUTLIER.toInt()) {
            return OUTLIER;
        } else if (type == CUSTOM.toInt()) {
            return CUSTOM;
        } else {
            return INVALID;
        }
    }

    public static TaskType getType(String type) {
        for (TaskType taskType : TaskType.values()) {
            if (taskType.toString().equalsIgnoreCase(type)) {
                return taskType;
            }
        }

        return null;
    }
}
