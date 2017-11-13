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

import com.sec.processing.framework.task.model.NaiveBayesModel;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NaiveBayesModelTest {
    @Test
    public void testAPIs() {

        NaiveBayesModel model = new NaiveBayesModel();

        Assert.assertNotNull(model.getDefaultParam());

        Assert.assertEquals(model.getName(), "NaiveBayes");
        Assert.assertEquals(model.getType().toInt(), TaskType.CLASSIFICATION.toInt());
    }

    @Test
    public void testGaussianCalculate() {
        /**
         * Sample Data Reference From "https://en.wikipedia.org/wiki/Naive_Bayes_classifier"
         *
         * @param args
         */
        String[] labels = {"male", "female"};
        double[][] rVectors = {
                {6.0, 175.0, 11.0},
                {4.0, 132.0, 7.0}
        };
        double[][] means = {
                {5.855, 176.25, 11.25},
                {5.4175, 132.5, 7.5}
        };

        double[][] variances = {
                {3.5033e-02, 1.2292e+02, 9.1667e-01},
                {9.7225e-02, 5.5833e+02, 1.6667e+00}
        };

        double[] probabilities = {0.5, 0.5};

        String[] records = {
                "{\"FIELD1\":6,\"FIELD2\":177,\"FIELD3\":12}",
                "{\"FIELD1\":5,\"FIELD2\":120,\"FIELD3\":5}",
                "{\"FIELD1\":4,\"FIELD2\":131,\"FIELD3\":7}",
                "{\"FIELD1\":6,\"FIELD2\":180,\"FIELD3\":11}"
        };
        String outKey = "/records/*/Out";

        ArrayList<ArrayList<Double>> meanList = new ArrayList();
        for (int index = 0; index < means.length; index++) {
            ArrayList<Double> tList = new ArrayList<>();
            for (int index2 = 0; index2 < means[index].length; index2++) {
                tList.add(means[index][index2]);
            }
            meanList.add(tList);
        }
        ArrayList<ArrayList<Double>> varianceList = new ArrayList();
        for (int index = 0; index < variances.length; index++) {
            ArrayList<Double> tList = new ArrayList<>();
            for (int index2 = 0; index2 < variances[index].length; index2++) {
                tList.add(variances[index][index2]);
            }
            varianceList.add(tList);
        }
        ArrayList<ArrayList<Double>> vectorList = new ArrayList();
        for (int index = 0; index < rVectors.length; index++) {
            ArrayList<Double> tList = new ArrayList<>();
            for (int index2 = 0; index2 < rVectors[index].length; index2++) {
                tList.add(rVectors[index][index2]);
            }
            vectorList.add(tList);
        }
        ArrayList<Double> possibilityList = new ArrayList<>();
        for (int index2 = 0; index2 < probabilities.length; index2++) {
            possibilityList.add(probabilities[index2]);
        }

        TaskModelParam classInfo = new TaskModelParam();
        List<String> clsNames = Arrays.asList(labels);

        classInfo.put("labels", clsNames);
        classInfo.put("possibilities", possibilityList);
        classInfo.put("rVectors", vectorList);
        classInfo.put("means", meanList);
        classInfo.put("variances", varianceList);

        TaskModelParam kernel = new TaskModelParam();
        kernel.put("type", "gaussian");
        kernel.put("bandwidth", 5.5);

        TaskModelParam params = new TaskModelParam();
        params.put("classInfo", classInfo);
        params.put("kernel", kernel);

        List<String> target = new ArrayList<>();
        for (int index = 1; index < 4; index++) {
            target.add("/records/FIELD" + index);
        }
        params.put("target", target);
        List<String> output = new ArrayList<>();
        output.add(outKey);
        params.put("outputKey", output);

        NaiveBayesModel model = new NaiveBayesModel();
        model.setParam(params);

        System.out.println("======== GAUSSIAN");
        for (int i = 0; i < records.length; i++) {
            //for (int i = 0; i < 2; i++) {
            DataSet dataSet = DataSet.create();
            dataSet.addRecord(records[i]);
            dataSet = model.calculate(dataSet, target, output);

            if (dataSet == null) {
                System.out.println("ignored input " + i);
            } else {

                System.out.println(dataSet.toString());
            }
        }
    }

    @Test
    public void testExponentialCalculate() {
        /**
         * Sample Data Reference From "https://en.wikipedia.org/wiki/Naive_Bayes_classifier"
         *
         * @param args
         */
        String[] labels = {"male", "female"};
        double[][] rVectors = {
                {6.0, 175.0, 11.0},
                {4.0, 132.0, 7.0}
        };
        double[][] means = {
                {5.855, 176.25, 11.25},
                {5.4175, 132.5, 7.5}
        };

        double[][] variances = {
                {3.5033e-02, 1.2292e+02, 9.1667e-01},
                {9.7225e-02, 5.5833e+02, 1.6667e+00}
        };

        double[] probabilities = {0.5, 0.5};

        String[] records = {
                "{\"FIELD1\":6,\"FIELD2\":177,\"FIELD3\":12}",
                "{\"FIELD1\":5,\"FIELD2\":120,\"FIELD3\":5}",
                "{\"FIELD1\":4,\"FIELD2\":131,\"FIELD3\":7}",
                "{\"FIELD1\":6,\"FIELD2\":180,\"FIELD3\":11}"
        };
        String outKey = "/records/*/Out";

        ArrayList<ArrayList<Double>> meanList = new ArrayList();
        for (int index = 0; index < means.length; index++) {
            ArrayList<Double> tList = new ArrayList<>();
            for (int index2 = 0; index2 < means[index].length; index2++) {
                tList.add(means[index][index2]);
            }
            meanList.add(tList);
        }
        ArrayList<ArrayList<Double>> varianceList = new ArrayList();
        for (int index = 0; index < variances.length; index++) {
            ArrayList<Double> tList = new ArrayList<>();
            for (int index2 = 0; index2 < variances[index].length; index2++) {
                tList.add(variances[index][index2]);
            }
            varianceList.add(tList);
        }
        ArrayList<ArrayList<Double>> vectorList = new ArrayList();
        for (int index = 0; index < rVectors.length; index++) {
            ArrayList<Double> tList = new ArrayList<>();
            for (int index2 = 0; index2 < rVectors[index].length; index2++) {
                tList.add(rVectors[index][index2]);
            }
            vectorList.add(tList);
        }
        ArrayList<Double> possibilityList = new ArrayList<>();
        for (int index2 = 0; index2 < probabilities.length; index2++) {
            possibilityList.add(probabilities[index2]);
        }

        TaskModelParam classInfo = new TaskModelParam();
        List<String> clsNames = Arrays.asList(labels);

        classInfo.put("labels", clsNames);
        classInfo.put("possibilities", possibilityList);
        classInfo.put("rVectors", vectorList);
        classInfo.put("means", meanList);
        classInfo.put("variances", varianceList);

        TaskModelParam kernel = new TaskModelParam();
        kernel.put("type", "exponential");
        kernel.put("bandwidth", 5.5);

        TaskModelParam params = new TaskModelParam();
        params.put("classInfo", classInfo);
        params.put("kernel", kernel);

        List<String> target = new ArrayList<>();
        for (int index = 1; index < 4; index++) {
            target.add("/records/FIELD" + index);
        }
        params.put("target", target);
        List<String> output = new ArrayList<>();
        output.add(outKey);
        params.put("outputKey", output);

        NaiveBayesModel model = new NaiveBayesModel();
        model.setParam(params);

        System.out.println("======== EXPONENTIAL ");
        for (int i = 0; i < records.length; i++) {
            //for (int i = 0; i < 2; i++) {
            DataSet dataSet = DataSet.create();
            dataSet.addRecord(records[i]);
            dataSet = model.calculate(dataSet, target, output);

            if (dataSet == null) {
                System.out.println("ignored input " + i);
            } else {

                System.out.println(dataSet.toString());
            }
        }
    }

    @Test
    public void testKernelDensityCalculate() {
        /**
         * Sample Data Reference From "https://en.wikipedia.org/wiki/Naive_Bayes_classifier"
         *
         * @param args
         */
        String[] labels = {"male", "female"};
        double[][] rVectors = {
                {6.0, 175.0, 11.0},
                {4.0, 132.0, 7.0}
        };
        double[][] means = {
                {5.855, 176.25, 11.25},
                {5.4175, 132.5, 7.5}
        };

        double[][] variances = {
                {3.5033e-02, 1.2292e+02, 9.1667e-01},
                {9.7225e-02, 5.5833e+02, 1.6667e+00}
        };

        double[] probabilities = {0.5, 0.5};

        String[] records = {
                "{\"FIELD1\":6,\"FIELD2\":177,\"FIELD3\":12}",
                "{\"FIELD1\":5,\"FIELD2\":120,\"FIELD3\":5}",
                "{\"FIELD1\":4,\"FIELD2\":131,\"FIELD3\":7}",
                "{\"FIELD1\":6,\"FIELD2\":180,\"FIELD3\":11}"
        };
        String outKey = "/records/*/Out";

        ArrayList<ArrayList<Double>> meanList = new ArrayList();
        for (int index = 0; index < means.length; index++) {
            ArrayList<Double> tList = new ArrayList<>();
            for (int index2 = 0; index2 < means[index].length; index2++) {
                tList.add(means[index][index2]);
            }
            meanList.add(tList);
        }
        ArrayList<ArrayList<Double>> varianceList = new ArrayList();
        for (int index = 0; index < variances.length; index++) {
            ArrayList<Double> tList = new ArrayList<>();
            for (int index2 = 0; index2 < variances[index].length; index2++) {
                tList.add(variances[index][index2]);
            }
            varianceList.add(tList);
        }
        ArrayList<ArrayList<Double>> vectorList = new ArrayList();
        for (int index = 0; index < rVectors.length; index++) {
            ArrayList<Double> tList = new ArrayList<>();
            for (int index2 = 0; index2 < rVectors[index].length; index2++) {
                tList.add(rVectors[index][index2]);
            }
            vectorList.add(tList);
        }
        ArrayList<Double> possibilityList = new ArrayList<>();
        for (int index2 = 0; index2 < probabilities.length; index2++) {
            possibilityList.add(probabilities[index2]);
        }

        TaskModelParam classInfo = new TaskModelParam();
        List<String> clsNames = Arrays.asList(labels);

        classInfo.put("labels", clsNames);
        classInfo.put("possibilities", possibilityList);
        classInfo.put("rVectors", vectorList);
        classInfo.put("means", meanList);
        classInfo.put("variances", varianceList);

        TaskModelParam kernel = new TaskModelParam();
        kernel.put("type", "kernel_density_est");
        kernel.put("bandwidth", 5.5);

        TaskModelParam params = new TaskModelParam();
        params.put("classInfo", classInfo);
        params.put("kernel", kernel);

        List<String> target = new ArrayList<>();
        for (int index = 1; index < 4; index++) {
            target.add("/records/FIELD" + index);
        }
        params.put("target", target);
        List<String> output = new ArrayList<>();
        output.add(outKey);
        params.put("outputKey", output);

        NaiveBayesModel model = new NaiveBayesModel();
        model.setParam(params);

        System.out.println("======== KERNEL DENSITY ESTIMATION");
        for (int i = 0; i < records.length; i++) {
            //for (int i = 0; i < 2; i++) {
            DataSet dataSet = DataSet.create();
            dataSet.addRecord(records[i]);
            dataSet = model.calculate(dataSet, target, output);

            if (dataSet == null) {
                System.out.println("ignored input " + i);
            } else {

                System.out.println(dataSet.toString());
            }
        }
    }
}
