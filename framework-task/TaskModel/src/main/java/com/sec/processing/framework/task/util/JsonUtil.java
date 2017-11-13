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
package com.sec.processing.framework.task.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public final class JsonUtil {

    private JsonUtil() { }
    public static JsonElement parse(String json) {
        JsonParser jsonParser = new JsonParser();
        return jsonParser.parse(json);
    }

    public static JsonArray makeArray(double[] data) {
        JsonArray array = new JsonArray();
        for (int i = 0; i < data.length; i++) {
            array.add(data[i]);
        }
        return array;
    }

    public static JsonArray makeArray(double[][] data) {
        JsonArray array = new JsonArray();
        for (int i = 0; i < data.length; i++) {
            array.add(makeArray(data[i]));
        }
        return array;
    }

    public static JsonArray makeArray(Object[] data) {
        JsonArray array = new JsonArray();
        for (int i = 0; i < data.length; i++) {
            if (data[i] instanceof Number) {
                array.add((Number) data[i]);
            } else if (data[i] instanceof String) {
                array.add((String) data[i]);
            } else if (data[i] instanceof Character) {
                array.add((Character) data[i]);
            } else if (data[i] instanceof Boolean) {
                array.add((Boolean) data[i]);
            } else if (data[i] instanceof JsonElement) {
                array.add((JsonElement) data[i]);
            } else if (data[i] instanceof JsonArray) {
                array.addAll((JsonArray) data[i]);
            } else {
                // Unsupported data type. Try casting to String
                array.add((String) data[i]);
            }
        }
        return array;
    }

    public static JsonArray makeArray(Object[][] data) {
        JsonArray array = new JsonArray();
        for (int i = 0; i < data.length; i++) {
            array.add(makeArray(data[i]));
        }
        return array;
    }
}
