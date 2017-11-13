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
import com.sec.processing.framework.task.function.CommonFunction;
import com.sec.processing.framework.task.function.SigmoidFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes linear/logistic regression.
 * <p>
 * Flink requires this class to be serializable.
 */
public abstract class RegressionModel extends AbstractTaskModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegressionModel.class);

    // Distance functions could be extended..
    public static final int LINEAR = 1;
    public static final int LOGISTIC = 2;

    private int algorithmType;
    private double error;
    private double[] weights = null;
    private TaskType taskType;
    private String name;

    public RegressionModel(TaskType type, String name, int algoType) {
        this.taskType = type;
        this.name = name;
        this.algorithmType = algoType;
        this.weights = null;
    }
    @Override
    public TaskType getType() {
        return this.taskType;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public RegressionModel setWeights(double[] weights) {
        if (weights.length > 0) {
            this.weights = weights.clone();
        }

        return this;
    }

    public int getAlgorithmType() {
        return this.algorithmType;
    }

    public void setError(double error) {
        this.error = error;
    }

    public void setAlgorithmType(int algorithmType) {
        this.algorithmType = algorithmType;
    }

    public void setAlgorithmType(String algorithmType) {
        if (algorithmType.equalsIgnoreCase("logistic")) {
            this.setAlgorithmType(LOGISTIC);
        } else {
            this.setAlgorithmType(LINEAR);
        }
    }

    private double calWithLinearFunction(Double[] dataInstance) {
        double result = 0.0;

        result = CommonFunction.product(dataInstance, weights);
        result += this.error;

        return result;
    }

    private double calWithLogisticFunction(Double[] dataInstance) {
        double result = 0.0;

        result = CommonFunction.product(dataInstance, weights);
        result += this.error;

        return SigmoidFunction.calculate(result, SigmoidFunction.TYPESIGMOID.LOGISTIC);
    }

    @Override
    public void setParam(TaskModelParam params) {

        if (params.containsKey("weights")) {
            List<Number> weights = (List<Number>) params.get("weights");
            this.setWeights(TaskModelParam.transformToNativeDouble1DArray(weights));
        }

        if (params.containsKey("error")) {
            setError(Double.parseDouble(params.get("error").toString()));
        }
        if (params.containsKey("type")) {
            this.setAlgorithmType(params.get("type").toString());
        }
    }

    @Override
    public TaskModelParam getDefaultParam() {
        TaskModelParam params = new TaskModelParam();
        Double[] trainedWeights = {
                0.2, -0.7
        };
        ArrayList<Double> trainedW = new ArrayList<>();
        for (Double w : trainedWeights) {
            trainedW.add(Double.valueOf(w));
        }
        params.put("weights", trainedW);
        params.put("error", new String("-1.7"));
        params.put("type", new String("logistic"));

        return params;
    }

    @Override
    public DataSet calculate(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {
        LOGGER.info("[Regression] Entering calculation");
        List<Number> scores = new ArrayList<>();
        List<List<Number>> vectors = new ArrayList<>();
        for (int index = 0; index < inRecordKeys.size(); index++) {
            List<Number> values = in.getValue(inRecordKeys.get(index), List.class);
            vectors.add(values);
        }
        if (vectors.size() == inRecordKeys.size()) {
            for (int loop = 0; loop < vectors.get(0).size(); loop++) {
                Double[] dataInstance = new Double[inRecordKeys.size()];
                for (int index = 0; index < inRecordKeys.size(); index++) {
                    dataInstance[index] = vectors.get(index).get(loop).doubleValue();
                }
                if (this.algorithmType == LINEAR) {
                    scores.add(calWithLinearFunction(dataInstance));
                    in.setValue(outRecordKeys.get(0), scores);
                } else if (this.algorithmType == LOGISTIC) {
                    scores.add(calWithLogisticFunction(dataInstance));
                    in.setValue(outRecordKeys.get(0), scores);
                }
            }
        } else {
            LOGGER.error("[Regression] Feature value extraction from given data failed~!!");
        }

        LOGGER.info("[Regression] Returning calculation result");
        return in;
    }
}
