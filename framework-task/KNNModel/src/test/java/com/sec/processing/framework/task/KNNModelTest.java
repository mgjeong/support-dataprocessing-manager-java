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

import com.sec.processing.framework.task.model.KNNModel;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KNNModelTest {
    private Double[][] dataSetArray = {
            {0.756889761, 2.573637533},
            {0.402033512, 2.526481237},
            {3.855560383, 2.531932706},
            {1.35883268, 0.241088287},
            {2.837857366, 1.894422598},
            {1.536304037, 0.046769476},
            {3.387866811, 2.859321489},
            {1.762943855, 1.334423851},
            {2.357919775, 1.214759511},
            {1.86323899, 0.627732521},
            {0.954912379, 3.129197735},
            {2.640268956, 3.273798397},
            {2.947745395, 3.431808291},
            {2.423633693, 0.432469884},
            {2.921055194, 3.683068},
            {2.201198552, 2.742034361},
            {0.65302676, 1.947683155},
            {1.18661701, 3.066075124},
            {3.86212364, 2.814342501},
            {0.260480046, 0.683826701},
            {13.59641808, 15.92543114},
            {15.62487816, 15.81528752},
            {14.31103999, 15.92260315},
            {15.95628022, 14.70841379},
            {13.6087172, 15.9100728},
            {14.87136568, 13.4063605},
            {13.11170685, 15.58756861},
            {14.30721578, 14.82479752},
            {14.58996297, 13.86339264},
            {14.91926335, 14.78265079}
    };

    @Test
    public void testParams() {
        KNNModel model = new KNNModel();
        Assert.assertEquals(model.getType(), TaskType.OUTLIER);
        Assert.assertEquals(model.getName(), "outlier_knn");
    }

    @Test
    public void testManhattanCase() {

        KNNModel model = new KNNModel();
        TaskModelParam params = new TaskModelParam();//model.getDefaultParam();
        params.put("knn", 2);
        params.put("threshold", 2.0);
        params.put("distanceMeasure", "MANHATTAN");
        ArrayList<ArrayList<Double>> trainedSet = new ArrayList();
        for(int index = 0 ; index < dataSetArray.length; index++) {
            ArrayList<Double> tList = new ArrayList<>();
            for (int index2 = 0; index2 < dataSetArray[index].length; index2++) {
                tList.add(dataSetArray[index][index2]);
            }
            trainedSet.add(tList);
        }
        params.put("clusterVectors", trainedSet);
        List<String> target = new ArrayList<>();
        target.add("/records/A");
        target.add("/records/B");
        List<String> output = new ArrayList<>();
        output.add("/records/*/outlier_knn");

        model.setParam(params);

        String[] records = {
                "{\"A\": 5.0, \"B\": 13.0}",
                "{\"A\": 3.7, \"B\": 2.9}",
                "{\"A\": 10.0, \"B\": 3.0}"
        };
        System.out.println("===================== DISTANCE - MANHATTAN =====================");
        System.out.println("===================== RECORD BY RECORD =====================");
        for (int i = 0; i < records.length; i++) {
            DataSet dataSet = DataSet.create();
            dataSet.addRecord(records[i]);

            dataSet = model.calculate(dataSet, target, output);
            if (dataSet == null) {
                System.out.println("ignored input " + i);
            } else {

                System.out.println(dataSet.toString());
            }
        }
        DataSet dataSet1 = DataSet.create();
        for (int i = 0; i < records.length; i++) {
            dataSet1.addRecord(records[i]);
        }
        System.out.println("===================== SET OF RECORDS =====================");
        dataSet1 = model.calculate(dataSet1, target, output);
        if (dataSet1 != null) {
            System.out.println(dataSet1.toString());
        }
    }

    @Test
    public void testEuclideanCase() {

        ArrayList<ArrayList<Double>> trainedSet = new ArrayList();
        for(int index = 0 ; index < dataSetArray.length; index++) {
            ArrayList<Double> tList = new ArrayList<>();
            for (int index2 = 0; index2 < dataSetArray[index].length; index2++) {
                tList.add(dataSetArray[index][index2]);
            }
            trainedSet.add(tList);
        }

        KNNModel model = new KNNModel();
        TaskModelParam params = new TaskModelParam();//model.getDefaultParam();
        params.put("knn", 2);
        params.put("threshold", 2.0);
        params.put("distanceMeasure", "defaultDistance");
        params.put("clusterVectors", trainedSet);
        List<String> target = new ArrayList<>();
        target.add("/records/A");
        target.add("/records/B");
        List<String> output = new ArrayList<>();
        output.add("/outlier_knn");

        model.setParam(params);

        Assert.assertEquals(model.getDistanceMeasure(), KNNModel.Distance.EUCLIDEAN);

        String[] records = {
                "{\"A\": 5.0, \"B\": 13.0}",
                "{\"A\": 3.7, \"B\": 2.9}",
                "{\"A\": 10.0, \"B\": 3.0}"
        };
        System.out.println("===================== DISTANCE - EUCLIDEAN =====================");
        System.out.println("===================== RECORD BY RECORD =====================");
        for (int i = 0; i < records.length; i++) {
            DataSet dataSet = DataSet.create();
            dataSet.addRecord(records[i]);

            dataSet = model.calculate(dataSet, target, output);
            if (dataSet == null) {
                System.out.println("ignored input " + i);
            } else {

                System.out.println(dataSet.toString());
            }
        }
        DataSet dataSet1 = DataSet.create();
        for (int i = 0; i < records.length; i++) {
            dataSet1.addRecord(records[i]);
        }
        System.out.println("===================== SET OF RECORDS =====================");
        dataSet1 = model.calculate(dataSet1, target, output);
        if (dataSet1 != null) {
            System.out.println(dataSet1.toString());
        }

    }

    @Test
    public void testAbsRelativeCase() {

        ArrayList<ArrayList<Double>> trainedSet = new ArrayList();
        for(int index = 0 ; index < dataSetArray.length; index++) {
            ArrayList<Double> tList = new ArrayList<>();
            for (int index2 = 0; index2 < dataSetArray[index].length; index2++) {
                tList.add(dataSetArray[index][index2]);
            }
            trainedSet.add(tList);
        }

        KNNModel model = new KNNModel();
        TaskModelParam params = new TaskModelParam();//model.getDefaultParam();
        params.put("knn", 2);
        params.put("threshold", 2.0);
        params.put("clusterVectors", trainedSet);
        params.put("distanceMeasure", "ABS_RELATIVE");
        List<String> target = new ArrayList<>();
        target.add("/records/A");
        target.add("/records/B");
        params.put("target", target);
        List<String> output = new ArrayList<>();
        output.add("/outlier_knn");
        params.put("outputKey", output);
        model.setParam(params);

        Assert.assertEquals(model.getDistanceMeasure(), KNNModel.Distance.ABS_RELATIVE);

        String[] records = {
                "{\"A\": 5.0, \"B\": 13.0}",
                "{\"A\": 3.7, \"B\": 2.9}",
                "{\"A\": 10.0, \"B\": 3.0}"
        };
        System.out.println("===================== DISTANCE - ABS_RELATIVE =====================");
        System.out.println("===================== RECORD BY RECORD =====================");
        for (int i = 0; i < records.length; i++) {
            DataSet dataSet = DataSet.create();
            dataSet.addRecord(records[i]);

            dataSet = model.calculate(dataSet, target, output);
            if (dataSet == null) {
                System.out.println("ignored input " + i);
            } else {

                System.out.println(dataSet.toString());
            }
        }
        DataSet dataSet1 = DataSet.create();
        for (int i = 0; i < records.length; i++) {
            dataSet1.addRecord(records[i]);
        }
        System.out.println("===================== SET OF RECORDS =====================");
        dataSet1 = model.calculate(dataSet1, target, output);
        if (dataSet1 != null) {
            System.out.println(dataSet1.toString());
        }
    }
}
