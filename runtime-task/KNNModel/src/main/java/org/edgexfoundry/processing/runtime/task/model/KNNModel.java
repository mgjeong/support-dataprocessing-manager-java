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
import org.edgexfoundry.processing.runtime.task.function.DistanceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class KNNModel extends AbstractTaskModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(KNNModel.class);

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
    private Double threshold = Double.NEGATIVE_INFINITY;
    private Integer mKnn = 2;

    public KNNModel() {
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
        return "outlier_knn";
    }

    public Distance getDistanceMeasure() {
        return this.distanceMeasure;
    }

    @Override
    /**
     * Override function
     * Get default parameter values for the reference
     */
    public TaskModelParam getDefaultParam() {
        TaskModelParam params = new TaskModelParam();

        params.put("clusterVectors", Distance.EUCLIDEAN.name());

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
            trainKNNModel();
        }
    }

    private void trainKNNModel() {
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

    private Double getDistance(Double[] first, Double[] second) {
        // calculate absolute relative distance
        Double distance = 0.0;
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

    private void calculateDistance(Double[] testInstance) {
        // update the table with distances among training instances and the current test instance:
        Integer i = 0;
        for (Double[] trainInstance : this.trainInstances) {
            this.distTable[i][this.numInstances] = getDistance(trainInstance, testInstance);
            this.distTable[this.numInstances][i] = this.distTable[i][this.numInstances];
            i++;
        }
        this.distTable[this.numInstances][this.numInstances] = -1.0;

        // sort the distances
        for (i = 0; i < this.numInstances + 1; i++) {
            this.distSorted[i] = sortedIndices(this.distTable[i]);
        }
    }

    public Double getScore(Double[] testInstance) {
        calculateDistance(testInstance);
        return getAverageKnnDistance(this.numInstances, this.mKnn);
    }

    private Double getAverageKnnDistance(Integer index, Integer knn) {
        Double avgDistance = 0.0;

        for (Integer i = 0; i < knn; i++) {
            if (index != this.distSorted[index][i]) {
                avgDistance += this.distTable[index][this.distSorted[index][i]];
            }
        }

        LOGGER.debug("avgDistance : " + avgDistance + " knn : " + knn);
        return (avgDistance / knn);
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
