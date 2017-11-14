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
package org.edgexfoundry.processing.runtime.task.model;

import org.edgexfoundry.processing.runtime.task.AbstractTaskModel;
import org.edgexfoundry.processing.runtime.task.DataSet;
import org.edgexfoundry.processing.runtime.task.TaskModelParam;
import org.edgexfoundry.processing.runtime.task.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class NaiveBayesModel extends AbstractTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NaiveBayesModel.class);

    // Distance functions could be extended..
    public static final int GAUSSIAN = 1;
    public static final int EXPONENTIAL = 2;
    public static final int KERNEL_DENSITY_EST = 3;
    // Set of Classes
    private String[] mClasses;
    // Set of probabilities on each classes
    private double[] mClassProbabilities;
    // The mean of values in each attributes(features) belonging to each classes
    private double[][] mMeans;
    // The variance of values in each attributes(features) belonging to each classes
    private double[][] mVariances;
    // The random vectors for the kernel density estimation
    private double[][] mVectors;
    // The smoothing Parameter(bandwidth) for the kernel density estimation
    private double mBandwidth;
    private int mAlgorithmType;
    /**
     * Constructor
     */
    public NaiveBayesModel() {
        mClasses = null;
        mClassProbabilities = null;
        mMeans = null;
        mVariances = null;
        mVectors = null;
        mBandwidth = 0.0;
    }

    @Override
    /**
     * Override function
     * Get type of this task
     */
    public TaskType getType() {
        return TaskType.CLASSIFICATION;
    }

    @Override
    /**
     * Override function
     * Get name of this task
     */
    public String getName() {
        return "NaiveBayes";
    }

    @Override
    /**
     * Override function
     * Get default parameter values for the reference
     */
    public TaskModelParam getDefaultParam() {
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

        String outKey = "/Out";

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
            target.add("/FIELD" + index);
        }
        params.put("target", target);
        List<String> output = new ArrayList<>();
        output.add(outKey);
        params.put("outputKey", output);

        return params;
    }

    @Override
    /**
     * Override function
     * Set parameter values which are required for processing this task
     */
    public void setParam(TaskModelParam params) {
        if (params.containsKey("kernel")) {
            HashMap<String, Object> kernel = (HashMap<String, Object>) params.get("kernel");
            String type = (String) kernel.get("type");
            if (type.compareToIgnoreCase("gaussian") == 0) {
                this.mAlgorithmType = NaiveBayesModel.GAUSSIAN;
            } else if (type.compareToIgnoreCase("exponential") == 0) {
                this.mAlgorithmType = NaiveBayesModel.EXPONENTIAL;
            } else if (type.compareToIgnoreCase("kernel_density_est") == 0) {
                //this.mAlgorithmType = NaiveBayesModel.KERNEL_DENSITY_EST;
                LOGGER.debug("KERNEL-DENSITY-EST type not supported Yet, Setting to GAUSSIAN");
                this.mAlgorithmType = NaiveBayesModel.GAUSSIAN;
            }
            if (kernel.containsKey("bandwidth")) {
                this.mBandwidth = Double.parseDouble(kernel.get("bandwidth").toString());
            }
        }
        if (params.containsKey("classInfo")) {
            HashMap<String, Object> clsInfo = (HashMap<String, Object>) params.get("classInfo");
            if (clsInfo.containsKey("labels")) {
                List<String> lList = (List<String>) clsInfo.get("labels");
                mClasses = lList.toArray(new String[lList.size()]);
            }
            if (clsInfo.containsKey("possibilities")) {
                this.mClassProbabilities = TaskModelParam.transformToNativeDouble1DArray((List<Number>) clsInfo.get("possibilities"));
            }
            if (clsInfo.containsKey("rVectors")) {
                this.mVectors = TaskModelParam.transformToNativeDouble2DArray((List<List<Number>>) clsInfo.get("rVectors"));
            }
            if (clsInfo.containsKey("means")) {
                this.mMeans = TaskModelParam.transformToNativeDouble2DArray((List<List<Number>>) clsInfo.get("means"));
            }
            if (clsInfo.containsKey("variances")) {
                this.mVariances = TaskModelParam.transformToNativeDouble2DArray((List<List<Number>>) clsInfo.get("variances"));
            }
        }
    }

    private double calculateGaussianProbabilityDensity(double feature, double mean, double variance) {
        double value = (Math.pow((feature - mean), 2) / variance);
        double exponent = ((-1.0) / 2.0) * (value);
        double front = (1.0 / Math.sqrt(2.0 * Math.PI * variance));

        return (front * (Math.exp(exponent)));
    }

    private double calculateExponentialProbabilityDensity(double feature, double mean) {
        double result = (1.0 / mean) * Math.exp((-1.0) * (feature / mean));
        return result;
    }

    /**
     * Calculate Classification result
     *
     * @param dataInstance
     * @return
     */
    public double[] calculateScores(double[] dataInstance) {
        // List of probabilities on given dataInstance for each class
        double[] result = new double[this.mClasses.length];
        // Probability of given X(features) on class C(cNum) == P(X|Ci)
        double temp = 1.0;
        // Iterate for each classes
        try {
            for (int cNum = 0; cNum < mClasses.length; cNum++) {
                // Iterate for each attributes of dataInstance(feature vectors)
                temp = 1.0;
                for (int iter = 0; iter < mMeans[cNum].length; iter++) {
                    // Calcuate the conditional Probabilities P(X|Ci) where X = features, Ci = cNum
                    if (this.mAlgorithmType == GAUSSIAN) {
                        temp *= calculateGaussianProbabilityDensity(dataInstance[iter],
                                mMeans[cNum][iter], mVariances[cNum][iter]);
                    } else if (this.mAlgorithmType == EXPONENTIAL) {
                        temp *= calculateExponentialProbabilityDensity(dataInstance[iter], mMeans[cNum][iter]);
                    } else if (this.mAlgorithmType == KERNEL_DENSITY_EST) {
                        double value = 0.0, z = 0.0;
                        // if bandwidth value not provided, then use variance value
                        double denominator = this.mVectors[cNum].length;
                        if (this.mBandwidth != 0.0) {
                            denominator *= this.mBandwidth;
                        } else {
                            denominator *= this.mVariances[cNum][iter];
                        }
                        double head = (1.0 / denominator);

                        for (int attr = 0; attr < this.mVectors[cNum].length; attr++) {
                            double denom = this.mBandwidth;
                            if (this.mBandwidth == 0.0) {
                                denom = mVariances[cNum][iter];
                            }
                            z = ((dataInstance[iter] - this.mVectors[cNum][attr]) / denom);
                            value += calculateGaussianProbabilityDensity(z, this.mMeans[cNum][iter], this.mVariances[cNum][iter]);
                        }
                        temp *= (head * value);
                    } else {
                        // more options can be added
                        temp = 0.0;
                    }
                }
                // P(Ci|X) = P(X|Ci) * P(Ci)
                result[cNum] = temp * mClassProbabilities[cNum];
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            LOGGER.error("Array Length not matched ===========");
            LOGGER.error("Means : " + mMeans.length);
            LOGGER.error("Variances : " + mVariances.length);
            LOGGER.error("Data : " + dataInstance.length);
            LOGGER.error("====================================");

            result = null;
        } catch (UnsupportedOperationException e) {
            LOGGER.error("[ " + this.mAlgorithmType + " ] Not Supported");
            result = null;
        }
        // Return list of the Probability values of given data instance(feature vector) on each classes.
        return result;
    }

    @Override
    /**
     * Calculation Classification algorithm(NB) and apply result into the received data set
     */
    public DataSet calculate(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {
        LOGGER.info("[NaiveBayes] String calculation");
        List<List<Number>> vectors = new ArrayList<>();
        for (int index = 0; index < inRecordKeys.size(); index++) {
            List<Number> values = in.getValue(inRecordKeys.get(index), List.class);
            vectors.add(values);
        }
        if (vectors.size() == inRecordKeys.size()) {
            for (int loop = 0; loop < vectors.get(0).size(); loop++) {
                double[] dataInstance = new double[inRecordKeys.size()];
                for (int index = 0; index < inRecordKeys.size(); index++) {
                    dataInstance[index] = vectors.get(index).get(loop).doubleValue();
                }

                double[] scores = calculateScores(dataInstance);
                double max = 0.0;
                int pos = -1;
                for (int index = 0; index < scores.length; index++) {
                    if (scores[index] > max) {
                        max = scores[index];
                        pos = index;
                    }
                }
                in.setValue(outRecordKeys.get(0), mClasses[pos]);
                //System.out.println("MSG : " + in.toString());
            }
        } else {
            LOGGER.error("[NaiveBayes]Feature value extraction from given data failed~!!");
        }

        LOGGER.info("[NaiveBayes] Returning calculation result");
        return in;
    }
}
