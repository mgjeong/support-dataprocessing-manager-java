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
package org.edgexfoundry.support.dataprocessing.runtime.data.model.job;

import java.io.Serializable;

public enum JobState implements Serializable {
    CREATE(0), RUNNING(1), STOPPED(2), INVALID(3);

    private int state;

    JobState(int state) {
        setState(state);
    }

    @Override
    public String toString() {
        return this.name().toUpperCase();
    }

    public void setState(int state) {
        this.state = state;
    }

    public static JobState getState(String state) {
        JobState ret = null;
        for (JobState jobState : JobState.values()) {
            if (jobState.toString().equalsIgnoreCase(state)) {
                ret = jobState;
                break;
            }
        }
        return ret;
    }
}

