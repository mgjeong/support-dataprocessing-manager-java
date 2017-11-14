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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TaskModelParamTest {

    @Test
    public void createTest() {

        TaskModelParam test = new TaskModelParam();
        test.put("test", "test");

        TaskModelParam param = TaskModelParam.create(test.toString());

        Assert.assertEquals(param.get("test"), "test");

        Assert.assertNotNull(param.toString());

        TaskModelParam param2 = TaskModelParam.create(null);
        Assert.assertNull(param2);

    }

    @Test
    public void extractDouble2DArrayTest() {

        Double[] values = {1.0, 2.0, 3.0};

        List<Number> list = new ArrayList<>();

        for (Double value : values) {
            list.add(value);
        }

        List<List<Number>> dList = new ArrayList<>();
        for (int loop = 0; loop < 5; loop++) {
            dList.add(list);
        }

        Double[][] testList = TaskModelParam.transformToDouble2DArray(dList);

        for (int index = 0; index < values.length; index++) {
            Assert.assertEquals(testList[0][index].doubleValue(), values[index].doubleValue(), 0);
        }

    }

    @Test
    public void extractDouble1DArrayTest() {

        Double[] values = {1.0, 2.0, 3.0};

        List<Number> list = new ArrayList();

        for (Double value : values) {
            list.add(value);
        }

        Double[] testList = TaskModelParam.transformToDouble1DArray(list);

        for (int index = 0; index < values.length; index++) {
            Assert.assertEquals(testList[index].doubleValue(), values[index].doubleValue(), 0);
        }
    }

    @Test
    public void extractNativeDouble2DArrayTest() {

        Double[] values = {1.0, 2.0, 3.0};

        List<Number> list = new ArrayList<>();

        for (Double value : values) {
            list.add(value);
        }

        List<List<Number>> dList = new ArrayList<>();
        for (int loop = 0; loop < 5; loop++) {
            dList.add(list);
        }

        double[][] testList = TaskModelParam.transformToNativeDouble2DArray(dList);

        for (int index = 0; index < values.length; index++) {
            Assert.assertEquals(testList[0][index], values[index].doubleValue(), 0);
        }

    }

    @Test
    public void extractNativeDouble1DArrayTest() {

        Double[] values = {1.0, 2.0, 3.0};

        List<Number> list = new ArrayList();

        for (Double value : values) {
            list.add(value);
        }

        double[] testList = TaskModelParam.transformToNativeDouble1DArray(list);

        for (int index = 0; index < values.length; index++) {
            Assert.assertEquals(testList[index], values[index].doubleValue(), 0);
        }
    }

    @Test
    public void extractNativeDouble2DArrayTest2() {

        Double[] values = {1.0, 2.0, 3.0};

        List<Number> list = new ArrayList<>();

        for (Double value : values) {
            list.add(value);
        }

        List<List<Number>> dList = new ArrayList<>();
        for (int loop = 0; loop < 5; loop++) {
            dList.add(list);
        }

        double[][] testList = TaskModelParam.extractDouble2DArray(dList);

        for (int index = 0; index < values.length; index++) {
            Assert.assertEquals(testList[0][index], values[index].doubleValue(), 0);
        }

    }

    @Test
    public void extractNativeDouble1DArrayTest2() {

        Double[] values = {1.0, 2.0, 3.0};

        List<Number> list = new ArrayList();

        for (Double value : values) {
            list.add(value);
        }

        double[] testList = TaskModelParam.extractDouble1DArray(list);

        for (int index = 0; index < values.length; index++) {
            Assert.assertEquals(testList[index], values[index].doubleValue(), 0);
        }
    }


    @Test
    public void extractInt1DArrayTest() {

        Integer[] values = {1, 2, 3};

        List<Number> list = new ArrayList();

        for (Integer value : values) {
            list.add(value);
        }

        Integer[] testList = TaskModelParam.transformToInt1DArray(list);

        for (int index = 0; index < values.length; index++) {
            Assert.assertEquals(testList[index].intValue(), values[index].intValue(), 0);
        }
    }

    @Test
    public void extractNativeInt1DArrayTest() {

        Integer[] values = {1, 2, 3};

        List<Number> list = new ArrayList();

        for (Integer value : values) {
            list.add(value);
        }

        int[] testList = TaskModelParam.transformToNativeInt1DArray(list);

        for (int index = 0; index < values.length; index++) {
            Assert.assertEquals(testList[index], values[index].intValue(), 0);
        }
    }

    @Test
    public void extractNativeInt2DArrayTest() {

        Integer[] values = {1, 2, 3};

        List<Number> list = new ArrayList<>();

        for (Integer value : values) {
            list.add(value);
        }

        List<List<Number>> dList = new ArrayList<>();
        for (int loop = 0; loop < 5; loop++) {
            dList.add(list);
        }

        int[][] testList = TaskModelParam.transformToNativeInt2DArray(dList);

        for (int index = 0; index < values.length; index++) {
            Assert.assertEquals(testList[0][index], values[index].intValue(), 0);
        }

    }

    @Test
    public void extractInt2DArrayTest() {

        Integer[] values = {1, 2, 3};

        List<Number> list = new ArrayList<>();

        for (Integer value : values) {
            list.add(value);
        }

        List<List<Number>> dList = new ArrayList<>();
        for (int loop = 0; loop < 5; loop++) {
            dList.add(list);
        }

        Integer[][] testList = TaskModelParam.transformToInt2DArray(dList);

        for (int index = 0; index < values.length; index++) {
            Assert.assertEquals(testList[0][index].intValue(), values[index].intValue(), 0);
        }

    }

    @Test
    public void toStringTest() {

        TaskModelParam param = new TaskModelParam();

        param.toString();

        param.put("test", "test");
        System.out.println(param.toString());

        param.put("/test/", ":/test/giulrd6yqv345tq3");
        System.out.println(param.toString());
    }
}
