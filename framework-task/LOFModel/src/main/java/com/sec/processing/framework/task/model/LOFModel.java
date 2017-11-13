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

import com.sec.processing.framework.task.AbstractTaskModel;
import com.sec.processing.framework.task.DataSet;
import com.sec.processing.framework.task.TaskModelParam;
import com.sec.processing.framework.task.TaskType;
import com.sec.processing.framework.task.function.DistanceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class LOFModel extends AbstractTaskModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(LOFModel.class);

    public enum Distance {
        ABS_RELATIVE, EUCLIDEAN, MANHATTAN;

        public static Distance parse(String s) {
            for (Distance d : Distance.values()) {
                if (d.name().equalsIgnoreCase(s)) {
                    return d;
                }
            }
            return Distance.EUCLIDEAN; // default
        }
    }

    /**
     * The training instances.
     */
    private Double[][] trainInstances;
    private Integer numAttributes, numInstances;

    /**
     * .
     * The distances among instances.
     */
    private Double[][] distTable;

    /**
     * Indices of the sorted distance.
     */
    private Integer[][] distSorted;

    /**
     * The minimum values for training instances.
     */
    private Double[] minTrain;

    /**
     * The maximum values training instances.
     */
    private Double[] maxTrain;

    private Distance distanceMeasure = Distance.EUCLIDEAN;
    private Double threshold = 0.0;
    private Integer mKnn = 2;

    public LOFModel() {
        threshold = Double.NEGATIVE_INFINITY;
        distanceMeasure = Distance.EUCLIDEAN;
    }

    @Override
    /**
     * Override function
     * Get type of this task
     */
    public TaskType getType() {
        return TaskType.OUTLIER;
    }

    @Override
    /**
     * Override function
     * Get name of this task
     */
    public String getName() {
        return "outlier_lof";
    }

    @Override
    /**
     * Override function
     * Get default parameter values for the reference
     */
    public TaskModelParam getDefaultParam() {
        TaskModelParam params = new TaskModelParam();

        params.put("distanceMeasure", Distance.EUCLIDEAN.name());

        Double[][] trainInstances = {
                {0.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}, {3.0, 0.0}
        };
        ArrayList<ArrayList<Double>> trainedSet = new ArrayList();
        for (int index = 0; index < trainInstances.length; index++) {
            ArrayList<Double> tList = new ArrayList<>();
            for (int index2 = 0; index2 < trainInstances[index].length; index2++) {
                tList.add(trainInstances[index][index2]);
            }
            trainedSet.add(tList);
        }
        params.put("clusterVectors", trainedSet);

        params.put("threshold", this.threshold);
        params.put("knn", this.mKnn);
        List<String> target = new ArrayList<>();
        target.add("/records/A");
        target.add("/records/B");
        params.put("target", target);
        List<String> output = new ArrayList<>();
        output.add("/outlier_lof");
        params.put("outputKey", output);
        return params;
    }

    @Override
    /**
     * Override function
     * Set parameter values which are required for processing this task
     */
    public void setParam(TaskModelParam params) {
        if (params.containsKey("distanceMeasure")) {
            this.distanceMeasure = Distance.parse((String) params.get("distanceMeasure"));
        }
        if (params.containsKey("knn")) {
            this.mKnn = Integer.parseInt(params.get("knn").toString());
        }
        if (params.containsKey("threshold")) {
            this.threshold = Double.parseDouble(params.get("threshold").toString());
        }
        if (params.containsKey("clusterVectors")) {
            List<List<Number>> temp = ((List<List<Number>>) params.get("clusterVectors"));
            this.trainInstances = TaskModelParam.transformToDouble2DArray(temp);
        }
        trainLOFModel();
    }

    private void trainLOFModel() {
        this.numInstances = Integer.valueOf(this.trainInstances.length);
        this.numAttributes = Integer.valueOf(this.trainInstances[0].length);

        // get the bounds for numeric attributes of training instances:
        this.minTrain = new Double[this.numAttributes];
        this.maxTrain = new Double[this.numAttributes];

        for (int i = 0; i < this.numAttributes; i++) {
            this.minTrain[i] = Double.POSITIVE_INFINITY;
            this.maxTrain[i] = Double.NEGATIVE_INFINITY;
            for (Double[] instance : this.trainInstances) {
                if (instance[i] < this.minTrain[i]) {
                    minTrain[i] = instance[i];
                }
                if (instance[i] > this.maxTrain[i]) {
                    maxTrain[i] = instance[i];
                }
            }
        }

        // fill the table with distances among training instances
        this.distTable = new Double[this.numInstances + 1][this.numInstances + 1];
        this.distSorted = new Integer[this.numInstances + 1][this.numInstances + 1];

        for (int i = 0; i < this.trainInstances.length; i++) {
            for (int j = 0; j < this.trainInstances.length; j++) {
                if (i == j) {
                    this.distTable[i][j] = -1.0;
                } else {
                    this.distTable[i][j] = getDistance(this.trainInstances[i], this.trainInstances[j]);
                }
            }
        }
    }

    private double getDistance(Double[] first, Double[] second) {
        // calculate absolute relative distance
        double distance = 0;
        switch (distanceMeasure) {
            case MANHATTAN:
                for (int i = 0; i < this.numAttributes; i++) {
                    distance += Math.abs(first[i] - second[i]);
                }
                break;
            case ABS_RELATIVE:
                distance = DistanceFunction.distABSRelative(first, second, this.maxTrain, this.minTrain);
                break;
            case EUCLIDEAN:
                distance = DistanceFunction.distEuclidean(first, second);
                break;
            default:
                break;
        }
        return distance;
    }

    private void calculateDistanceToTest(Double[] testInstance) {
        // update the table with distances among training instances and the current test instance:
        int i = 0;
        for (Double[] trainInstance : trainInstances) {
            distTable[i][numInstances] = getDistance(trainInstance, testInstance);
            distTable[numInstances][i] = distTable[i][numInstances];
            i++;
        }
        distTable[numInstances][numInstances] = -1.0;

        // sort the distances
        for (i = 0; i < numInstances + 1; i++) {
            distSorted[i] = sortedIndices(distTable[i]);
        }
    }

    public Double getScore(Double[] testInstance) {
        calculateDistanceToTest(testInstance);
        return getLofIdx(numInstances, this.mKnn);
    }

    public Double[] getTrainingScores(Integer kNN) {
        // update the table with distances among training instances and a fake test instance
        for (int i = 0; i < numInstances; i++) {
            distTable[i][numInstances] = Double.MAX_VALUE;
            distSorted[i] = sortedIndices(distTable[i]);
            distTable[numInstances][i] = Double.MAX_VALUE;
        }

        Double[] res = new Double[numInstances];
        for (int idx = 0; idx < numInstances; idx++) {
            res[idx] = getLofIdx(idx, kNN);
        }
        return res;
    }

    private Integer[] sortedIndices(Double[] array) {
        int[] temp = IntStream.range(0, array.length)
                .boxed().sorted((i, j) -> (int) (1000 * (array[i].doubleValue() - array[j].doubleValue())))
                .mapToInt(ele -> ele).toArray();

        Integer[] sortResult = new Integer[temp.length];
        //System.out.println("temp : "+temp.length + " sorted : "+sortResult.length);
        for (int iter = 0; iter < temp.length; iter++) {
            sortResult[iter] = Integer.valueOf(temp[iter]);
        }
        return sortResult;
    }

    private Double getLofIdx(Integer index, Integer kNN) {
        // get the number of nearest neighbors for the current test instance:
        Integer numNN = getNNCount(kNN, index);

        // get LOF for the current test instance:
        Double lof = 0.0;
        for (int i = 1; i <= numNN; i++) {
            double lrdi = getLocalReachDensity(kNN, index);
            if (lrdi != 0) {
                lof += getLocalReachDensity(kNN, distSorted[index][i]) / lrdi;
            }
        }
        lof /= numNN;
        return lof;
    }

    private Integer getNNCount(Integer kNN, Integer instIndex) {
        Integer numNN = kNN;
        // if there are more neighbors with the same distance, take them too
        for (int i = kNN; i < distTable.length - 1; i++) {
            if (distTable[instIndex][distSorted[instIndex][i]] == distTable[instIndex][distSorted[instIndex][i + 1]]) {
                numNN++;
            } else {
                break;
            }
        }
        return numNN;
    }

    private Double getLocalReachDensity(Integer kNN, Integer instIndex) {
        // get the number of nearest neighbors:
        Integer numNN = getNNCount(kNN, instIndex);
        Double lrd = 0.0;
        for (int i = 1; i <= numNN; i++) {
            lrd += getReachDistance(kNN, instIndex, distSorted[instIndex][i]);
        }
        if (lrd != 0) {
            lrd = numNN / lrd;
        }
        return lrd;
    }

    private Double getReachDistance(Integer kNN, Integer firstIndex, Integer secondIndex) {
        // max({distance to k-th nn of second}, distance(first, second))
        Double reachDist = distTable[firstIndex][secondIndex];
        Integer numNN = getNNCount(kNN, secondIndex);
        if (distTable[secondIndex][distSorted[secondIndex][numNN]] > reachDist) {
            reachDist = distTable[secondIndex][distSorted[secondIndex][numNN]];
        }
        return reachDist;
    }

    @Override
    public DataSet calculate(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {

        List<Double> scorelist = new ArrayList<>();
        List<List<Number>> vectors = new ArrayList<>();
        for (int index = 0; index < inRecordKeys.size(); index++) {
            List<Number> values = in.getValue(inRecordKeys.get(index), List.class);
            vectors.add(values);
        }
        if (vectors.size() == inRecordKeys.size()) {
            for (int attr = 0; attr < vectors.get(0).size(); attr++) {
                // 1. Make a records for k-nn calculation
                Double[] testInstance = new Double[vectors.size()];
                for (int index = 0; index < inRecordKeys.size(); index++) {
                    testInstance[index] = vectors.get(index).get(attr).doubleValue();
                }
                // 2. Calculate k-nn score on each records
                Double temp = getScore(testInstance);
                scorelist.add(temp);
            }
        }
        // 3. Insert result into the DataSet instance
        //ex: { "Outlier_LOF" : [0.0, ....] }
        if (scorelist.size() > 0) {
            in.setValue(outRecordKeys.get(0), scorelist);
            if(this.threshold != Double.NEGATIVE_INFINITY) {
                in.setValue("/threshold", this.threshold);
            }
        } else {
            LOGGER.debug("No Outlier Detected");
        }
        return in;
    }
}
