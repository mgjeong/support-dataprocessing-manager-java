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
package org.edgexfoundry.support.dataprocessing.runtime.task;

import java.util.List;

public abstract class AbstractTaskModel implements TaskModel {
    private List<String> inRecordKeys;
    private List<String> outRecordKeys;

    public AbstractTaskModel() {
        // This default constructor (with no argument) is required for dynamic instantiation from TaskFactory.
    }

    @Override
    public void setInRecordKeys(List<String> inRecordKeys) {
        this.inRecordKeys = inRecordKeys;
    }

    @Override
    public void setOutRecordKeys(List<String> outRecordKeys) {
        this.outRecordKeys = outRecordKeys;
    }

    @Override
    public DataSet calculate(DataSet in) {
        return calculate(in, this.inRecordKeys, this.outRecordKeys);

        // This could be an alternative.
        /*List<Object> input = new ArrayList<>();
        for (String key : this.inRecordKeys) {
            input.add(in.getValue(key, Object.class));
        }
        Map<String, Object> output = calculate(input, this.outRecordKeys);
        for (String key : this.outRecordKeys) {
            in.setValue(key, output.get(key));
        }
        return in;*/
    }

    public abstract DataSet calculate(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys);
}
