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

import org.edgexfoundry.support.dataprocessing.runtime.task.model.LinearRegressionModel;
import org.edgexfoundry.support.dataprocessing.runtime.task.model.LogisticRegressionModel;
import org.edgexfoundry.support.dataprocessing.runtime.task.model.RegressionModel;
import org.edgexfoundry.support.dataprocessing.runtime.task.model.RegressionModel.RegressionType;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RegressionModelTest {

    @Test
    public void testConstructor() {

        RegressionModel model = new LinearRegressionModel();

        Assert.assertEquals(TaskType.REGRESSION, model.getType());


        RegressionModel model2 = new LogisticRegressionModel();

        Assert.assertEquals(TaskType.REGRESSION, model2.getType());
    }

    @Test
    public void testGetProcessingType() {

        RegressionModel model = new LinearRegressionModel();

        Assert.assertEquals(TaskType.REGRESSION, model.getType());


        RegressionModel model2 = new LogisticRegressionModel();

        Assert.assertEquals(TaskType.REGRESSION, model2.getType());
    }

    @Test
    public void testGetName() {

        TaskModel model1 = new LinearRegressionModel();

        Assert.assertEquals("regression-linear", model1.getName());

        RegressionModel model2 = new LogisticRegressionModel();

        Assert.assertEquals("regression-logistic", model2.getName());
    }

    @Test
    public void testSetParam() {
        RegressionModel model1 = new LinearRegressionModel();
        RegressionModel model2 = new LogisticRegressionModel();

        TaskModelParam param = new TaskModelParam();
        double[] weights = {1.0, 2.0, 3.0, 4.0};
        ArrayList<Double> trainedWeight = new ArrayList<>();
        for (double w : weights) {
            trainedWeight.add(Double.valueOf(w));
        }
        param.put("weights", trainedWeight);
        param.put("error", "5.67");

        param.put("type", "linear");
        model1.setParam(param);
        Assert.assertEquals(RegressionType.Linear, model1.getRegressionType());

        param.put("type", "logistic");
        model2.setParam(param);
        Assert.assertEquals(RegressionType.Logistic, model2.getRegressionType());
    }

    @Test
    public void testPrediction() {
        /*
         * @brief This weights has calculated with "ticdata2000.txt" and evaluated with "ticeval.txt" using R tool
         * @count 84
         * @link https://archive.ics.uci.edu/ml/datasets/Insurance+Company+Benchmark+%28COIL+2000%29
         */
        double[] trainedLinearWeights = {
                0.228758, -0.156367, -0.076053, 4.438103, -0.013772, -0.057729, -0.064135, -0.091184,
                0.079168, 0.232852, 0.184895, -0.162730, -0.143529, -0.197610, 0.013371, 0.055744, 0.063586,
                0.132661, 0.110600, -0.137898, 0.167547, 0.108086, 0.106888, -0.024476, -0.086525, -0.092372,
                -0.066006, -0.056369, 0.058882, 0.041577, -0.084595, -0.169737, -0.044816, -0.098127, -0.077932,
                -0.009962, -0.028224, -0.021372, -0.048000, -0.002622, 0.046431, -0.191930, 0.201900, -0.079681,
                -0.114898, -0.025332, -0.147640, 0.050427, 0.027520, -0.190859, 0.037761, -0.186507, -0.149085,
                -0.031291, 0.099267, -0.166712, 0.197141, -0.096736, -0.823573, -0.063322, 0.303512, -0.139935,
                0.214752, -0.245382, 0.188524, 0.427104, 0.088042, 0.672953, -0.171236, -0.429767, 0.179425, -0.316761,
                0.379640, 0.375668, 0.056403, -0.466760, 0.360497, -0.466893, 0.249074, 2.198648, 0.525941, 0.006336,
                0.143716, -0.741466
        };

        double[] actualData = {33, 6, 39, 9};
        double[][] testLRDataInstance = {
                {1, 4, 2, 8, 0, 6, 0, 3, 5, 0, 4, 1, 1, 8, 2, 2, 6, 0, 0, 1, 2, 6, 1, 0, 2, 1, 5, 3, 1, 8, 8, 1, 1, 8,
                        1, 3, 3, 3, 0, 0, 3, 3, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 1, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                {1, 3, 2, 2, 0, 5, 0, 4, 5, 2, 2, 1, 4, 5, 5, 4, 0, 5, 0, 0, 4, 0, 0, 4, 3, 0, 2, 1, 3, 6, 9, 0, 0, 7,
                        2, 1, 1, 5, 4, 0, 6, 8, 2, 0, 0, 6, 0, 4, 0, 0, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0, 0, 0, 1, 0, 0,
                        1, 0, 1, 0, 0, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                {1, 3, 3, 9, 1, 4, 2, 3, 5, 2, 3, 2, 3, 6, 2, 4, 4, 2, 1, 1, 3, 2, 2, 1, 1, 5, 2, 1, 1, 8, 6, 2, 2, 6,
                        3, 2, 4, 3, 1, 0, 3, 5, 2, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 4, 0, 0, 0, 0, 0, 1, 0, 0,
                        1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                {1, 2, 3, 3, 2, 3, 2, 4, 5, 4, 1, 2, 4, 4, 2, 4, 4, 2, 1, 1, 5, 1, 2, 3, 1, 3, 2, 2, 3, 6, 7, 2, 1, 7,
                        2, 2, 5, 3, 1, 0, 4, 4, 2, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 1, 0, 0,
                        1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}
        };

        String[] recordsLinear = {
                "{\"FIELD1\":1,\"FIELD2\":4,\"FIELD3\":2,\"FIELD4\":8,\"FIELD5\":0,\"FIELD6\":6,\"FIELD7\":0,"
                      +  "\"FIELD8\":3,\"FIELD9\":5,\"FIELD10\":0,\"FIELD11\":4,\"FIELD12\":1,\"FIELD13\":1,"
                      +  "\"FIELD14\":8,\"FIELD15\":2,\"FIELD16\":2,\"FIELD17\":6,\"FIELD18\":0,\"FIELD19\":0,"
                      +  "\"FIELD20\":1,\"FIELD21\":2,\"FIELD22\":6,\"FIELD23\":1,\"FIELD24\":0,\"FIELD25\":2,"
                      +  "\"FIELD26\":1,\"FIELD27\":5,\"FIELD28\":3,\"FIELD29\":1,\"FIELD30\":8,\"FIELD31\":8,"
                      +  "\"FIELD32\":1,\"FIELD33\":1,\"FIELD34\":8,\"FIELD35\":1,\"FIELD36\":3,\"FIELD37\":3,"
                      +  "\"FIELD38\":3,\"FIELD39\":0,\"FIELD40\":0,\"FIELD41\":3,\"FIELD42\":3,\"FIELD43\":1,"
                      +
                        "\"FIELD44\":0,\"FIELD45\":0,\"FIELD46\":0,\"FIELD47\":0,\"FIELD48\":0,\"FIELD49\":0,"
                      +  "\"FIELD50\":0,\"FIELD51\":0,\"FIELD52\":0,\"FIELD53\":0,\"FIELD54\":0,\"FIELD55\":0,"
                      +  "\"FIELD56\":0,\"FIELD57\":0,\"FIELD58\":4,\"FIELD59\":0,\"FIELD60\":0,\"FIELD61\":0,"
                      +  "\"FIELD62\":0,\"FIELD63\":0,\"FIELD64\":1,\"FIELD65\":0,\"FIELD66\":0,\"FIELD67\":0,"
                      +  "\"FIELD68\":0,\"FIELD69\":0,\"FIELD70\":0,\"FIELD71\":0,\"FIELD72\":0,\"FIELD73\":0,"
                      +  "\"FIELD74\":0,\"FIELD75\":0,\"FIELD76\":0,\"FIELD77\":0,\"FIELD78\":0,\"FIELD79\":1,"
                      +  "\"FIELD80\":0,\"FIELD81\":0,\"FIELD82\":0,\"FIELD83\":0,\"FIELD84\":0}",
                "{\"FIELD1\":1,\"FIELD2\":3,\"FIELD3\":2,\"FIELD4\":2,\"FIELD5\":0,\"FIELD6\":5,\"FIELD7\":0,"
                      +  "\"FIELD8\":4,\"FIELD9\":5,\"FIELD10\":2,\"FIELD11\":2,\"FIELD12\":1,\"FIELD13\":4,"
                      +  "\"FIELD14\":5,\"FIELD15\":5,\"FIELD16\":4,\"FIELD17\":0,\"FIELD18\":5,\"FIELD19\":0,"
                      +  "\"FIELD20\":0,\"FIELD21\":4,\"FIELD22\":0,\"FIELD23\":0,\"FIELD24\":4,\"FIELD25\":3,"
                      +  "\"FIELD26\":0,\"FIELD27\":2,\"FIELD28\":1,\"FIELD29\":3,\"FIELD30\":6,\"FIELD31\":9,"
                      +  "\"FIELD32\":0,\"FIELD33\":0,\"FIELD34\":7,\"FIELD35\":2,\"FIELD36\":1,\"FIELD37\":1,"
                      +  "\"FIELD38\":5,\"FIELD39\":4,\"FIELD40\":0,\"FIELD41\":6,\"FIELD42\":8,\"FIELD43\":2,"
                      +  "\"FIELD44\":0,\"FIELD45\":0,\"FIELD46\":6,\"FIELD47\":0,\"FIELD48\":4,\"FIELD49\":0,"
                      +  "\"FIELD50\":0,\"FIELD51\":0,\"FIELD52\":0,\"FIELD53\":0,\"FIELD54\":3,\"FIELD55\":0,"
                      +  "\"FIELD56\":0,\"FIELD57\":0,\"FIELD58\":4,\"FIELD59\":0,\"FIELD60\":0,\"FIELD61\":0,"
                      +  "\"FIELD62\":0,\"FIELD63\":0,\"FIELD64\":1,\"FIELD65\":0,\"FIELD66\":0,\"FIELD67\":1,"
                      +  "\"FIELD68\":0,\"FIELD69\":1,\"FIELD70\":0,\"FIELD71\":0,\"FIELD72\":0,\"FIELD73\":0,"
                      +  "\"FIELD74\":0,\"FIELD75\":2,\"FIELD76\":0,\"FIELD77\":0,\"FIELD78\":0,\"FIELD79\":0,"
                      +  "\"FIELD80\":0,\"FIELD81\":0,\"FIELD82\":0,\"FIELD83\":0,\"FIELD84\":0}",
                "{\"FIELD1\":1,\"FIELD2\":3,\"FIELD3\":3,\"FIELD4\":9,\"FIELD5\":1,\"FIELD6\":4,\"FIELD7\":2,"
                      +  "\"FIELD8\":3,\"FIELD9\":5,\"FIELD10\":2,\"FIELD11\":3,\"FIELD12\":2,\"FIELD13\":3,"
                      +  "\"FIELD14\":6,\"FIELD15\":2,\"FIELD16\":4,\"FIELD17\":4,\"FIELD18\":2,\"FIELD19\":1,"
                      +  "\"FIELD20\":1,\"FIELD21\":3,\"FIELD22\":2,\"FIELD23\":2,\"FIELD24\":1,\"FIELD25\":1,"
                      +  "\"FIELD26\":5,\"FIELD27\":2,\"FIELD28\":1,\"FIELD29\":1,\"FIELD30\":8,\"FIELD31\":6,"
                      +  "\"FIELD32\":2,\"FIELD33\":2,\"FIELD34\":6,\"FIELD35\":3,\"FIELD36\":2,\"FIELD37\":4,"
                      +  "\"FIELD38\":3,\"FIELD39\":1,\"FIELD40\":0,\"FIELD41\":3,\"FIELD42\":5,\"FIELD43\":2,"
                      +  "\"FIELD44\":0,\"FIELD45\":0,\"FIELD46\":6,\"FIELD47\":0,\"FIELD48\":0,\"FIELD49\":0,"
                      +  "\"FIELD50\":0,\"FIELD51\":0,\"FIELD52\":0,\"FIELD53\":0,\"FIELD54\":4,\"FIELD55\":0,"
                      +  "\"FIELD56\":0,\"FIELD57\":0,\"FIELD58\":4,\"FIELD59\":0,\"FIELD60\":0,\"FIELD61\":0,"
                      +  "\"FIELD62\":0,\"FIELD63\":0,\"FIELD64\":1,\"FIELD65\":0,\"FIELD66\":0,\"FIELD67\":1,"
                      +  "\"FIELD68\":0,\"FIELD69\":0,\"FIELD70\":0,\"FIELD71\":0,\"FIELD72\":0,\"FIELD73\":0,"
                      +  "\"FIELD74\":0,\"FIELD75\":1,\"FIELD76\":0,\"FIELD77\":0,\"FIELD78\":0,\"FIELD79\":1,"
                      +  "\"FIELD80\":0,\"FIELD81\":0,\"FIELD82\":0,\"FIELD83\":0,\"FIELD84\":0}",
                "{\"FIELD1\":1,\"FIELD2\":2,\"FIELD3\":3,\"FIELD4\":3,\"FIELD5\":2,\"FIELD6\":3,\"FIELD7\":2,"
                      +  "\"FIELD8\":4,\"FIELD9\":5,\"FIELD10\":4,\"FIELD11\":1,\"FIELD12\":2,\"FIELD13\":4,"
                      +  "\"FIELD14\":4,\"FIELD15\":2,\"FIELD16\":4,\"FIELD17\":4,\"FIELD18\":2,\"FIELD19\":1,"
                      +  "\"FIELD20\":1,\"FIELD21\":5,\"FIELD22\":1,\"FIELD23\":2,\"FIELD24\":3,\"FIELD25\":1,"
                      +  "\"FIELD26\":3,\"FIELD27\":2,\"FIELD28\":2,\"FIELD29\":3,\"FIELD30\":6,\"FIELD31\":7,"
                      +  "\"FIELD32\":2,\"FIELD33\":1,\"FIELD34\":7,\"FIELD35\":2,\"FIELD36\":2,\"FIELD37\":5,"
                      +  "\"FIELD38\":3,\"FIELD39\":1,\"FIELD40\":0,\"FIELD41\":4,\"FIELD42\":4,\"FIELD43\":2,"
                      +  "\"FIELD44\":0,\"FIELD45\":0,\"FIELD46\":5,\"FIELD47\":0,\"FIELD48\":0,\"FIELD49\":0,"
                      +  "\"FIELD50\":0,\"FIELD51\":0,\"FIELD52\":0,\"FIELD53\":0,\"FIELD54\":0,\"FIELD55\":0,"
                      +  "\"FIELD56\":0,\"FIELD57\":0,\"FIELD58\":3,\"FIELD59\":0,\"FIELD60\":0,\"FIELD61\":0,"
                      +  "\"FIELD62\":0,\"FIELD63\":0,\"FIELD64\":1,\"FIELD65\":0,\"FIELD66\":0,\"FIELD67\":1,"
                      +  "\"FIELD68\":0,\"FIELD69\":0,\"FIELD70\":0,\"FIELD71\":0,\"FIELD72\":0,\"FIELD73\":0,"
                      +  "\"FIELD74\":0,\"FIELD75\":0,\"FIELD76\":0,\"FIELD77\":0,\"FIELD78\":0,\"FIELD79\":1,"
                      +  "\"FIELD80\":0,\"FIELD81\":0,\"FIELD82\":0,\"FIELD83\":0,\"FIELD84\":0}"
        };

        double score = 0.0;
        double error = 1.235593;
        String outKey = "/Out";
        RegressionModel model = new LinearRegressionModel();
        TaskModelParam param = model.getDefaultParam();
        ArrayList<Double> trainedWeight = new ArrayList<>();
        for (double w : trainedLinearWeights) {
            trainedWeight.add(Double.valueOf(w));
        }
        param.put("weights", trainedWeight);
        param.put("type", "linear");
        param.put("error", error);
        List<String> target = new ArrayList<>();
        for (int index = 1; index < 85; index++) {
            target.add("/records/FIELD" + index);
        }
        param.put("target", target);
        List<String> output = new ArrayList<>();
        output.add(outKey);
        param.put("outputKey", output);
        model.setParam(param);

        System.out.println("======== LINEAR REGRESSION");
        for (int i = 0; i < recordsLinear.length; i++) {
            //for (int i = 0; i < 2; i++) {
            DataSet dataSet = DataSet.create();
            dataSet.addRecord(recordsLinear[i]);
            dataSet = model.calculate(dataSet, target, output);
            score = (double) dataSet.getValue(outKey, List.class).get(0);
            if (dataSet == null) {
                System.out.println("ignored input " + i);
            } else {

                System.out.println(dataSet.toString());
                System.out.println("Actual : " + actualData[i] + " Prediction : " + score);
                System.out.println(dataSet.toString());
                Assert.assertEquals(score, actualData[i], 5);
            }
        }

        /*
         * @brief This weights has calculated with "wdbc_train3.txt" and evaluated with "wdbc_train3.txt" using R tool
         * @count 30
         * @link https://archive.ics.uci.edu/ml/datasets/Breast+Cancer+Wisconsin+%28Prognostic%29
         */
        double[] trainedLogisticWeights = {
                -4.508e+15, -2.681e+13, 5.592e+14, 6.374e+12, 4.804e+16,
                -2.931e+16, 3.110e+14, 8.171e+15, -8.564e+15, -1.030e+16, 8.161e+15,
                -4.617e+14, -8.989e+14, 6.591e+12, -1.037e+16, 4.729e+16, -2.689e+16,
                1.993e+17, -4.806e+16, -4.364e+17, 1.102e+15, 1.390e+14, 6.421e+13,
                -9.950e+12, -6.379e+15, -7.493e+15, 6.282e+15, -7.308e+15, 1.098e+16,
                4.755e+16
        };
        double[] actualData2 = {1, 1, 0, 0};
        double[][] testLR2DataInstance = {
                {17.99, 10.38, 122.8, 1001, 0.1184, 0.2776, 0.3001, 0.1471, 0.2419, 0.07871, 1.095, 0.9053, 8.589,
                        153.4, 0.006399, 0.04904, 0.05373, 0.01587, 0.03003, 0.006193, 25.38, 17.33, 184.6, 2019,
                        0.1622, 0.6656, 0.7119, 0.2654, 0.4601, 0.1189},
                {20.57, 17.77, 132.9, 1326, 0.08474, 0.07864, 0.0869, 0.07017, 0.1812, 0.05667, 0.5435, 0.7339, 3.398,
                        74.08, 0.005225, 0.01308, 0.0186, 0.0134, 0.01389, 0.003532, 24.99, 23.41, 158.8, 1956, 0.1238,
                        0.1866, 0.2416, 0.186, 0.275, 0.08902},
                {13.08, 15.71, 85.63, 520, 0.1075, 0.127, 0.04568, 0.0311, 0.1967, 0.06811, 0.1852, 0.7477, 1.383,
                        14.67, 0.004097, 0.01898, 0.01698, 0.00649, 0.01678, 0.002425, 14.5, 20.49, 96.09, 630.5,
                        0.1312, 0.2776, 0.189, 0.07283, 0.3184, 0.08183},
                {9.504, 12.44, 60.34, 273.9, 0.1024, 0.06492, 0.02956, 0.02076, 0.1815, 0.06905, 0.2773, 0.9768, 1.909,
                        15.7, 0.009606, 0.01432, 0.01985, 0.01421, 0.02027, 0.002968, 10.23, 15.66, 65.13, 314.9,
                        0.1324, 0.1148, 0.08867, 0.06227, 0.245, 0.07773}
        };

        String[] recordLogistic = {
                "{\"FIELD1\":17.99,\"FIELD2\":10.38,\"FIELD3\":122.8,\"FIELD4\":1001,\"FIELD5\":0.1184,"
                      +  "\"FIELD6\":0.2776,\"FIELD7\":0.3001,\"FIELD8\":0.1471,\"FIELD9\":0.2419,"
                      +  "\"FIELD10\":0.07871,\"FIELD11\":1.095,\"FIELD12\":0.9053,\"FIELD13\":8.589,"
                      +  "\"FIELD14\":153.4,\"FIELD15\":0.006399,\"FIELD16\":0.04904,\"FIELD17\":0.05373,"
                      +  "\"FIELD18\":0.01587,\"FIELD19\":0.03003,\"FIELD20\":0.006193,\"FIELD21\":25.38,"
                      +  "\"FIELD22\":17.33,\"FIELD23\":184.6,\"FIELD24\":2019,\"FIELD25\":0.1622,\"FIELD26\":0.6656,"
                      +  "\"FIELD27\":0.7119,\"FIELD28\":0.2654,\"FIELD29\":0.4601,\"FIELD30\":0.1189}",
                "{\"FIELD1\":20.57,\"FIELD2\":17.77,\"FIELD3\":132.9,\"FIELD4\":1326,\"FIELD5\":0.08474,"
                      +  "\"FIELD6\":0.07864,\"FIELD7\":0.0869,\"FIELD8\":0.07017,\"FIELD9\":0.1812,"
                      +  "\"FIELD10\":0.05667,\"FIELD11\":0.5435,\"FIELD12\":0.7339,\"FIELD13\":3.398,"
                      +  "\"FIELD14\":74.08,\"FIELD15\":0.005225,\"FIELD16\":0.01308,\"FIELD17\":0.0186,"
                      +  "\"FIELD18\":0.0134,\"FIELD19\":0.01389,\"FIELD20\":0.003532,\"FIELD21\":24.99,"
                      +  "\"FIELD22\":23.41,\"FIELD23\":158.8,\"FIELD24\":1956,\"FIELD25\":0.1238,\"FIELD26\":0.1866,"
                      +  "\"FIELD27\":0.2416,\"FIELD28\":0.186,\"FIELD29\":0.275,\"FIELD30\":0.08902}",
                "{\"FIELD1\":13.08,\"FIELD2\":15.71,\"FIELD3\":85.63,\"FIELD4\":520,\"FIELD5\":0.1075,"
                      +  "\"FIELD6\":0.127,\"FIELD7\":0.04568,\"FIELD8\":0.0311,\"FIELD9\":0.1967,\"FIELD10\":0.06811,"
                      +  "\"FIELD11\":0.1852,\"FIELD12\":0.7477,\"FIELD13\":1.383,\"FIELD14\":14.67,"
                      +  "\"FIELD15\":0.004097,\"FIELD16\":0.01898,\"FIELD17\":0.01698,\"FIELD18\":0.00649,"
                      +  "\"FIELD19\":0.01678,\"FIELD20\":0.002425,\"FIELD21\":14.5,\"FIELD22\":20.49,"
                      +  "\"FIELD23\":96.09,\"FIELD24\":630.5,\"FIELD25\":0.1312,\"FIELD26\":0.2776,\"FIELD27\":0.189,"
                      +  "\"FIELD28\":0.07283,\"FIELD29\":0.3184,\"FIELD30\":0.08183}",
                "{\"FIELD1\":9.504,\"FIELD2\":12.44,\"FIELD3\":60.34,\"FIELD4\":273.9,\"FIELD5\":0.1024,"
                      +  "\"FIELD6\":0.06492,\"FIELD7\":0.02956,\"FIELD8\":0.02076,\"FIELD9\":0.1815,"
                      +  "\"FIELD10\":0.06905,\"FIELD11\":0.2773,\"FIELD12\":0.9768,\"FIELD13\":1.909,"
                      +  "\"FIELD14\":15.7,\"FIELD15\":0.009606,\"FIELD16\":0.01432,\"FIELD17\":0.01985,"
                      +  "\"FIELD18\":0.01421,\"FIELD19\":0.02027,\"FIELD20\":0.002968,\"FIELD21\":10.23,"
                      +  "\"FIELD22\":15.66,\"FIELD23\":65.13,\"FIELD24\":314.9,\"FIELD25\":0.1324,"
                      +  "\"FIELD26\":0.1148,\"FIELD27\":0.08867,\"FIELD28\":0.06227,\"FIELD29\":0.245,"
                      +  "\"FIELD30\":0.07773}"
        };

        System.out.println("======== LOGISTIC REGRESSION");
        TaskModelParam param2 = new TaskModelParam();
        RegressionModel model2 = new LogisticRegressionModel();

        trainedWeight.clear();
        for (double w : trainedLogisticWeights) {
            trainedWeight.add(Double.valueOf(w));
        }
        param2.put("weights", trainedWeight);
        param2.put("type", "logistic");
        param2.put("error", -1.704e+16);

        List<String> target2 = new ArrayList<>();
        for (int index = 1; index < 31; index++) {
            target2.add("/records/FIELD" + index);
        }
        param2.put("target", target2);
        List<String> output2 = new ArrayList<>();
        output2.add(outKey);
        param2.put("outputKey", output2);
        model2.setParam(param2);

        for (int i = 0; i < recordLogistic.length; i++) {

            DataSet dataSet = DataSet.create();
            dataSet.addRecord(recordLogistic[i]);
            dataSet = model2.calculate(dataSet, target2, output2);

            score = (double) dataSet.getValue(outKey, List.class).get(0);

            if (dataSet == null) {
                System.out.println("ignored input " + i);
            } else {

                System.out.println(dataSet.toString());
                System.out.println("Actual : " + actualData2[i] + " Prediction : " + score);
                System.out.println(dataSet.toString());
                Assert.assertEquals(score, actualData2[i], 5);
            }
        }
    }
}
