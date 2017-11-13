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
import com.google.gson.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;
import java.util.UUID;

public class JsonUtilTest {
    private double epsilon = 1e-7;
    private Random random = new Random();

    @Test
    public void test1DDoubleJsonArray() {
        double[] testData = new double[10];
        for (int i = 0; i < testData.length; i++) {
            testData[i] = random.nextDouble();
        }

        JsonArray array = JsonUtil.makeArray(testData);
        Assert.assertNotNull(array);
        Assert.assertEquals(array.size(), testData.length);

        // Check elements
        for (int i = 0; i < testData.length; i++) {
            Assert.assertEquals(array.get(i).getAsDouble(), testData[i], epsilon);
        }
    }

    @Test
    public void test2DDoubleJsonArray() {
        double[][] testData = new double[10][20];
        for (int i = 0; i < testData.length; i++) {
            for (int j = 0; j < testData[i].length; j++) {
                testData[i][j] = random.nextDouble();
            }
        }

        JsonArray array = JsonUtil.makeArray(testData);
        Assert.assertNotNull(array);
        Assert.assertEquals(array.size(), testData.length);

        // Check elements
        JsonArray subArray;
        for (int i = 0; i < testData.length; i++) {
            subArray = array.get(i).getAsJsonArray();
            Assert.assertNotNull(subArray);
            Assert.assertEquals(subArray.size(), testData[i].length);

            for (int j = 0; j < testData[i].length; j++) {
                Assert.assertEquals(subArray.get(j).getAsDouble(), testData[i][j], epsilon);
            }
        }
    }

    @Test
    public void test1DObjectJsonArray() {
        String[] testData = new String[10];
        for (int i = 0; i < testData.length; i++) {
            testData[i] = UUID.randomUUID().toString();
        }

        JsonArray array = JsonUtil.makeArray(testData);
        Assert.assertNotNull(array);
        Assert.assertEquals(array.size(), testData.length);

        // Check elements
        for (int i = 0; i < testData.length; i++) {
            Assert.assertEquals(array.get(i).getAsString(), testData[i]);
        }
    }

    @Test
    public void test2DObjectJsonArray() {
        String[][] testData = new String[10][20];
        for (int i = 0; i < testData.length; i++) {
            for (int j = 0; j < testData[i].length; j++) {
                testData[i][j] = UUID.randomUUID().toString();
            }
        }

        JsonArray array = JsonUtil.makeArray(testData);
        Assert.assertNotNull(array);
        Assert.assertEquals(array.size(), testData.length);

        // Check elements
        JsonArray subArray;
        for (int i = 0; i < testData.length; i++) {
            subArray = array.get(i).getAsJsonArray();
            Assert.assertNotNull(subArray);
            Assert.assertEquals(subArray.size(), testData[i].length);

            for (int j = 0; j < testData[i].length; j++) {
                Assert.assertEquals(subArray.get(j).getAsString(), testData[i][j]);
            }
        }
    }

    @Test
    public void test1DObjectNumJsonArray() {
        Number[] testData = new Number[10];
        for (int i = 0; i < testData.length; i++) {
            testData[i] = (Number) random.nextInt();
        }

        JsonArray array = JsonUtil.makeArray(testData);
        Assert.assertNotNull(array);
        Assert.assertEquals(array.size(), testData.length);

        // Check elements
        for (int i = 0; i < testData.length; i++) {
            Assert.assertEquals(array.get(i).getAsInt(), testData[i]);
        }
    }

    @Test
    public void test1DObjectChaJsonArray() {
        Character[] testData = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};

        JsonArray array = JsonUtil.makeArray(testData);
        Assert.assertNotNull(array);
        Assert.assertEquals(array.size(), testData.length);

        // Check elements
        for (int i = 0; i < testData.length; i++) {
            Assert.assertEquals((Character) array.get(i).getAsCharacter(), testData[i]);
        }
    }

    @Test
    public void test1DObjectBoolJsonArray() {
        Boolean[] testData = new Boolean[10];
        for (int i = 0; i < testData.length; i++) {
            testData[i] = random.nextBoolean();
        }

        JsonArray array = JsonUtil.makeArray(testData);
        Assert.assertNotNull(array);
        Assert.assertEquals(array.size(), testData.length);

        // Check elements
        for (int i = 0; i < testData.length; i++) {
            Assert.assertEquals(array.get(i).getAsBoolean(), testData[i]);
        }
    }


    @Test
    public void test1DObjectJsonEleJsonArray() {
        JsonElement jsonElement = new JsonObject();
        ((JsonObject) jsonElement).addProperty("foo", "bar");
        ((JsonObject) jsonElement).addProperty("foo2", 42);

        System.out.println(jsonElement.toString());

        JsonElement jsonElement1 = JsonUtil.parse("{\"foo\":\"bar\",\"foo2\":42}");
        System.out.println(jsonElement1.toString());

        Assert.assertEquals(jsonElement, jsonElement1);
    }
}
