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
package com.sec.processing.framework.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DataSet extends HashMap<String, Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSet.class);
    private static final String DIR_SEPARATOR = "/";

    private enum Meta {
        Id("dpfw-dataset-id"),
        Records("records");
        private final String key;

        Meta(String key) {
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }
    }

    private DataSet() {
    }

    public Record getStreamedRecord() {
        return getRecords().get(0);
    }

    public List<Record> getRecords() {
        return (List<Record>) this.get(Meta.Records.getKey());
    }

    public String getId() {
        return (String) this.get(Meta.Id.getKey());
    }

    public void addRecord(Record record) {
        if (record == null) {
            return;
        }

        getRecords().add(record);
    }

    public void addRecord(String json) {
        if (json == null || json.isEmpty()) {
            return;
        }

        addRecord(Record.create(json));
    }

    public boolean isBatch() {
        return getRecords().size() > 1;
    }


    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return super.toString();
    }

    private Object getValue(Object parent, String[] tokens, int index) {
        if (parent == null) {
            return null;
        }

        String token = tokens[index];

        if (index + 1 == tokens.length) {
            // terminating condition
            if (parent instanceof List) {
                List<Object> list = new ArrayList<>();
                for (Object p : (List) parent) {
                    list.add(getValue(p, tokens, index));
                }
                return list;
            } else if (parent instanceof Map) {
                return ((Map) parent).get(token);
            } else {
                throw new RuntimeException("Invalid type? Should not reach here?");
            }
        }

        if (token.equals("^") && parent instanceof List) {
            return getValue(((List) parent).get(0), tokens, index + 1);
        } else if (token.equals("$") && parent instanceof List) {
            return getValue(((List) parent).get(((List) parent).size() - 1), tokens, index + 1);
        } else if (parent instanceof List) {
            int newIndex = index;
            if (token.equals("*")) {
                newIndex += 1;
            }

            List<Object> list = new ArrayList<>();
            for (Object p : (List) parent) {
                list.add(getValue(p, tokens, newIndex));
            }
            return list;
        } else if (parent instanceof Map) {
            return getValue(((Map) parent).get(token), tokens, index + 1);
        } else {
            throw new RuntimeException("Invalid type? Should not reach here?");
        }
    }

    public <T> T getValue(String key, Class<T> clazz) {
        String[] tokens = key.split(DIR_SEPARATOR);

        Object o = this;
        o = getValue(o, tokens, 1);
        if (o == null) {
            return null;
        } else if (clazz.getClass().isInstance(o.getClass())) {
            return (T) o;
        } else {
            throw new RuntimeException(o + " cannot be type casted to " + clazz.getTypeName());
        }
    }

    private void setValue(Object parent, String[] tokens, int index, Object value) {
        if (parent == null) {
            throw new RuntimeException("No object exists for given key. key=" + tokens[index - 1]);
        }

        String token = tokens[index];
        if (index + 1 == tokens.length) {
            // terminating condition
            if (parent instanceof List) {
                List<Object> parentList = (List) parent;
                List<Object> valueList = null;
                if (value instanceof List) {
                    valueList = (List) value;
                } else if (parentList.size() == 1) { // Make temporary list for single element
                    valueList = new ArrayList<>();
                    valueList.add(value);
                }
                if (valueList == null || valueList.size() != parentList.size()) {
                    throw new RuntimeException("List size do not match. parent=" + parentList.size());
                }

                for (int j = 0; j < parentList.size(); j++) {
                    setValue(parentList.get(j), tokens, index, valueList.get(j));
                }
            } else if (parent instanceof Map) {
                ((Map) parent).put(token, value);
            } else {
                throw new RuntimeException("Invalid type? Should not reach here?");
            }
            return;
        }

        if (token.equals("^") && parent instanceof List) {
            setValue(((List) parent).get(0), tokens, index + 1, value);
        } else if (token.equals("$") && parent instanceof List) {
            setValue(((List) parent).get(((List) parent).size() - 1), tokens, index + 1, value);
        } else if (parent instanceof List) {
            List<Object> parentList = (List) parent;
            List<Object> valueList = null;
            if (value instanceof List) {
                valueList = (List) value;
            } else if (parentList.size() == 1) { // Make temporary list for single element
                valueList = new ArrayList<>();
                valueList.add(value);
            }
            if (valueList == null || valueList.size() != parentList.size()) {
                throw new RuntimeException("List size do not match. parent=" + parentList.size());
            }

            int newIndex = index;
            if (token.equals("*")) {
                newIndex += 1;
            }

            for (int j = 0; j < parentList.size(); j++) {
                setValue(parentList.get(j), tokens, newIndex, valueList.get(j));
            }
        } else if (parent instanceof Map) {
            setValue(((Map) parent).get(token), tokens, index + 1, value);
        } else {
            throw new RuntimeException("Invalid type? Should not reach here?");
        }
    }

    public void setValue(String key, Object value) {
        String[] tokens = key.split(DIR_SEPARATOR);

        String last = tokens[tokens.length - 1];
        if (last.isEmpty() || last.equals("*") || last.equals("$") || last.equals("^")) {
            throw new RuntimeException("Invalid token found. token=" + last);
        }

        setValue(this, tokens, 1, value);
    }

    public static final class Record extends HashMap<String, Object> {
        private Record() {
        }

        @Override
        public String toString() {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                LOGGER.error(e.getMessage(), e);
            }
            return super.toString();
        }

        public static Record create() {
            return create(null);
        }

        public static Record create(String json) {
            if (json == null || json.isEmpty()) {
                return new Record();
            }
            // Instantiate from string
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(json, new TypeReference<Record>() {
                });
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                return new Record(); // good idea?
            }
        }
    }

    public static DataSet create() {
        return create(null, null);
    }

    public static DataSet create(String json) {
        return create(null, json);
    }

    public static DataSet create(String id, String json) {
        DataSet dataSet = null;
        if (json == null || json.isEmpty()) {
            dataSet = new DataSet();
        } else {
            // Instantiate from string
            try {
                ObjectMapper mapper = new ObjectMapper();
                dataSet = mapper.readValue(json, new TypeReference<DataSet>() {
                });
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        if (dataSet == null) {
            return null;
        } else if (isFrameworkDataSet(dataSet)) {
            return dataSet;
        } else {
            dataSet = new DataSet();
            // generate id and records
            if (id == null || id.isEmpty()) {
                id = String.valueOf(System.currentTimeMillis()); // generate random id
            }
            dataSet.put(Meta.Id.getKey(), id);

            List<Record> records = new ArrayList<>();
            if (json != null && !json.isEmpty()) {
                records.add(Record.create(json));
            }
            dataSet.put(Meta.Records.getKey(), records);
            return dataSet;
        }
    }

    private static boolean isFrameworkDataSet(DataSet dataSet) {
        return dataSet.containsKey(Meta.Id.getKey())
                && dataSet.containsKey(Meta.Records.getKey());
    }
}
