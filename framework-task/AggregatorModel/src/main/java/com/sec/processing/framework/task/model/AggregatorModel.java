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

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.sec.processing.framework.task.AbstractTaskModel;
import com.sec.processing.framework.task.DataSet;
import com.sec.processing.framework.task.TaskModelParam;
import com.sec.processing.framework.task.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AggregatorModel extends AbstractTaskModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(AggregatorModel.class);

    private enum AggregateBy {
        Id,
        StoreFirst,
        StoreLast;

        public static AggregateBy toAggregateBy(String op) {
            for (AggregateBy by : AggregateBy.values()) {
                if (by.name().equalsIgnoreCase(op)) {
                    return by;
                }
            }
            return getDefault();
        }

        public static AggregateBy getDefault() {
            return StoreLast;
        }
    }

    private AggregateBy aggregateBy;
    private String[] keys;
    private final AggregatorStorage storage;

    public AggregatorModel() {
        this.storage = new AggregatorStorage();
    }

    @Override
    public void setParam(TaskModelParam param) {
        this.keys = param.get("keys").toString().split(" ");

        if (param.containsKey("aggregateBy")) {
            this.aggregateBy = AggregateBy.toAggregateBy(param.get("aggregateBy").toString());
        } else {
            this.aggregateBy = AggregateBy.getDefault();
        }
        this.storage.initialize(this.aggregateBy);
    }

    @Override
    public TaskType getType() {
        return TaskType.PREPROCESSING;
    }

    @Override
    public String getName() {
        return "Aggregator";
    }

    @Override
    public TaskModelParam getDefaultParam() {
        TaskModelParam defaultParam = new TaskModelParam();

        defaultParam.put("keys", "A B C");
        defaultParam.put("aggregateBy", AggregateBy.getDefault().name());

        return defaultParam;
    }

    @Override
    public DataSet calculate(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {
        /*
        JsonObject dataInstance = JsonUtil.parse(in.getStreamData().getValue()).getAsJsonObject();

        boolean newDataAdded = false;

        for (String key : this.keys) {
            JsonElement value = dataInstance.get(key);
            if (value == null) {
                continue;
            }

            newDataAdded = this.storage.putValue(in.getStreamData().getId(), key, value) || newDataAdded;
        }

        // Return if no new data is added.
        if (!newDataAdded) {
            return null;
        }

        // Store payloads.
        this.storage.putPayload(in.getStreamData().getId(), in.getStreamData().getPayloads());

        // Check if aggregated data is ready
        String result = peekAggregatedDataIfReady(in.getStreamData().getId());
        if (result == null) { // aggregation is not done yet
            return null;
        } else {
            Map<String, String> payload = this.storage.getPayload(in.getStreamData().getId());
            in.getStreamData().addPayloads(payload);
            in.getStreamData().setValue(result);
            this.storage.remove(in.getStreamData().getId()); // remove from values
            return in;
        }
        */
        throw new UnsupportedOperationException("Could you implement this for me, please?");
    }

    private String peekAggregatedDataIfReady(String id) {
        if (!this.storage.isAggregatedDataReady(id, keys)) {
            return null;
        }

        Map<String, JsonElement> sub = this.storage.getValue(id);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.length; i++) {
            JsonElement value = sub.get(keys[i]);

            if (value.isJsonPrimitive()) {
                JsonPrimitive primitive = (JsonPrimitive) value;
                if (primitive.isString()) {
                    sb.append(value.getAsString());
                } else {
                    sb.append(value.toString());
                }
            } else if (value.isJsonObject()) {
                sb.append(value.getAsJsonObject());
            } else if (value.isJsonArray()) {
                sb.append(value.getAsJsonArray());
            }

            if (i + 1 < keys.length) {
                sb.append("\t");
            }
        }

        return sb.toString();
    }

    private static final class AggregatorStorage implements Serializable {
        private static final String DEFAULT_KEY = "default";
        /**
         * .
         * Key: Stream Data ID
         * Value:
         * - Key: Json entry key
         * - Value: Json entry value
         */
        private final Map<String, Map<String, JsonElement>> values;

        /**
         * .
         * Key: Stream Data ID
         * Value: Stream Data payload map
         */
        private final Map<String, Map<String, String>> payloads;
        private AggregateBy aggregateBy = AggregateBy.getDefault();

        AggregatorStorage() {
            this.values = new ConcurrentHashMap<>();
            this.payloads = new ConcurrentHashMap<>();
        }

        public boolean putValue(String id, String key, JsonElement value) {
            if (this.aggregateBy != AggregateBy.Id) {
                id = DEFAULT_KEY;
            }

            Map<String, JsonElement> sub = this.values.get(id);
            if (sub == null) {
                sub = new ConcurrentHashMap<>();
                this.values.put(id, sub);
            }

            // Do not put values if key already exists
            if (this.aggregateBy == AggregateBy.StoreFirst
                    && sub.containsKey(key)) {
                return false;
            }

            sub.put(key, value);
            return true;
        }

        public void putPayload(String id, Map<String, String> p) {
            if (this.aggregateBy != AggregateBy.Id) {
                id = DEFAULT_KEY;
            }

            Map<String, String> subPayload = this.payloads.get(id);
            if (subPayload == null) {
                subPayload = new ConcurrentHashMap<>();
                this.payloads.put(id, subPayload);
            }

            subPayload.putAll(p);
        }

        public Map<String, JsonElement> getValue(String id) {
            if (this.aggregateBy != AggregateBy.Id) {
                id = DEFAULT_KEY;
            }

            return this.values.get(id);
        }

        public Map<String, String> getPayload(String id) {
            if (this.aggregateBy != AggregateBy.Id) {
                id = DEFAULT_KEY;
            }

            return this.payloads.get(id);
        }

        public boolean isAggregatedDataReady(String id, String[] keys) {
            if (this.aggregateBy != AggregateBy.Id) {
                id = DEFAULT_KEY;
            }

            Map<String, JsonElement> sub = this.values.get(id);
            return !(sub == null || sub.size() != keys.length);
        }

        public void initialize(AggregateBy aggregateBy) {
            this.values.clear();
            this.payloads.clear();
            this.aggregateBy = aggregateBy;
        }

        public void remove(String id) {
            if (this.aggregateBy != AggregateBy.Id) {
                id = DEFAULT_KEY;
            }

            this.values.remove(id);
            this.payloads.remove(id);
        }

        public int size() {
            return this.values.size();
        }
    }
}
