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
package org.edgexfoundry.support.dataprocessing.runtime.task.model;

import java.util.ArrayList;
import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.task.AbstractTaskModel;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModelParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskParam.UiFieldType;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.edgexfoundry.support.dataprocessing.runtime.task.function.CommonFunction;
import org.edgexfoundry.support.dataprocessing.runtime.task.function.SigmoidFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Computes linear/logistic regression. <p> Flink requires this class to be serializable.
 */
public abstract class RegressionModel extends AbstractTaskModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegressionModel.class);

  private TaskType taskType;
  private String name;

  @TaskParam(key = "weights", uiName = "Weights", uiType = UiFieldType.ARRAYNUMBER, tooltip = "Enter weights")
  protected List<Double> weights;

  @TaskParam(key = "error", uiName = "Error", uiType = UiFieldType.NUMBER, tooltip = "Enter error term")
  protected Double error;

  @TaskParam(key = "type", uiName = "Regression Type", uiType = UiFieldType.STRING, tooltip = "Select regression type")
  protected RegressionType regressionType;

  public enum RegressionType {
    Linear,
    Logistic
  }

  public RegressionModel(TaskType type, String name, RegressionType regressionType) {
    this.taskType = type;
    this.name = name;
    this.regressionType = regressionType;
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

  public RegressionType getRegressionType() {
    return this.regressionType;
  }

  public void setError(Double error) {
    this.error = error;
  }

  private double calWithLinearFunction(Double[] dataInstance) {
    double result = 0.0;

    double[] w = weights.stream().mapToDouble(Double::doubleValue).toArray();
    result = CommonFunction.product(dataInstance, w);
    result += this.error;

    return result;
  }

  private double calWithLogisticFunction(Double[] dataInstance) {
    double result = 0.0;

    double[] w = weights.stream().mapToDouble(Double::doubleValue).toArray();
    result = CommonFunction.product(dataInstance, w);
    result += this.error;

    return SigmoidFunction.calculate(result, SigmoidFunction.TYPESIGMOID.LOGISTIC);
  }

  @Override
  public void setParam(TaskModelParam params) {

    if (params.containsKey("weights")) {
      this.weights = (List<Double>) params.get("weights");
    }

    if (params.containsKey("error")) {
      setError(Double.parseDouble(params.get("error").toString()));
      this.error = (Double) params.get("error");
    }
    if (params.containsKey("type")) {
      this.regressionType = RegressionType.valueOf((String) params.get("type"));
    }
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
        if (this.regressionType == RegressionType.Linear) {
          scores.add(calWithLinearFunction(dataInstance));
          in.setValue(outRecordKeys.get(0), scores);
        } else if (this.regressionType == RegressionType.Logistic) {
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

  public TaskModelParam getDefaultParam() {
    TaskModelParam params = new TaskModelParam();
    List<Double> trainedWeights = new ArrayList<>();
    trainedWeights.add(0.2);
    trainedWeights.add(-0.7);
    params.put("weights", trainedWeights);
    params.put("error", -1.7);
    params.put("type", RegressionType.Logistic.name());

    return params;
  }
}
