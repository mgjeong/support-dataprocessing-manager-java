/*******************************************************************************
 * Copyright 2018 Samsung Electronics All Rights Reserved.
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.task.AbstractTaskModel;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModelParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskParam.UiFieldType;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.edgexfoundry.support.dataprocessing.runtime.task.function.ErrorFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Computes Error <p> Flink requires this class to be serializable.
 */
public class ErrorModel extends AbstractTaskModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(ErrorModel.class);

  @TaskParam(key = "algorithmType", uiName = "Algorithm Type",
      uiType = UiFieldType.ENUMSTRING, defaultValue = "MAE")
  private ErrorFunction.MEASURE algorithmType;

  @TaskParam(key = "observation", uiName = "Observation", uiType = UiFieldType.STRING)
  private String observation = null;

  @TaskParam(key = "windowOption", uiName = "Window Option",
      uiType = UiFieldType.ENUMSTRING, defaultValue = "TIME")
  private WindowOption windowOption;

  @TaskParam(key = "windowSize", uiName = "Window Size", uiType = UiFieldType.NUMBER, defaultValue = "2")
  private int mWindowSize = 2;

  private HashMap<String, LinkedList<Number>> targetList = null;
  private LinkedList<Number> observationList = null;

  public enum WindowOption {
    TIME, COUNT
  }

  /**
   * @desc Construtor of thismodel
   */
  public ErrorModel() {
    targetList = new HashMap<>();
    observationList = new LinkedList<>();
  }

  /**
   * @return TaskType.ERROR
   * @desc Get type of this model
   */
  @Override
  public TaskType getType() {
    return TaskType.ERROR;
  }

  /**
   * @return "error"
   * @desc Get name of this model
   */
  @Override
  public String getName() {
    return "error";
  }

  /**
   * @desc Set parameters for this model
   */
  @Override
  public void setParam(TaskModelParam params) {

    if (params.containsKey("type")) {
      this.algorithmType = ErrorFunction.MEASURE
          .valueOf(params.get("type").toString().toUpperCase());
    }
    if (params.containsKey("observation")) {
      this.observation = params.get("observation").toString();
    }
    if (params.containsKey("windowOption")) {
      this.windowOption = WindowOption.valueOf(params.get("windowOption").toString().toUpperCase());
    }
    if (params.containsKey("windowSize")) {
      this.mWindowSize = Integer.parseInt(params.get("windowSize").toString());
    }
  }

  private double getAverageError(Double[] targets, Double[] observe) {
    double error = 0.0;
    LOGGER.info("Target Size {} : Observe Size {}", targets.length, observe.length);
    if (targets.length == observe.length) {
      double[] pred = new double[targets.length];
      double[] obsv = new double[observe.length];

      for (int index = 0; index < targets.length; index++) {
        pred[index] = targets[index].doubleValue();
        obsv[index] = observe[index].doubleValue();
      }

      error = ErrorFunction.calculate(pred, obsv, this.algorithmType);

    } else {
      LOGGER.error("Length Not Match - Target {}, Observe {}", targets.length, observe.length);
    }
    return error;
  }

  /**
   * @param in : Data in json format to be processed
   * @param inRecordKeys : Target data key name
   * @param outRecordKeys : Data key name for processing result
   * @desc Calculate error value
   */
  @Override
  public DataSet calculate(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {
    LOGGER.info("[Error] Entering calculation");

    if (in.getRecords().size() < 1) {
      LinkedList<Number> value = null;
      Double observe = in.getValue(this.observation, Double.class);
      if (observe != null) {
        if (observationList.size() >= this.mWindowSize) {
          observationList.removeFirst();
        }
        observationList.addLast(observe);

        for (int index = 0; index < inRecordKeys.size(); index++) {
          Number predict = in.getValue(inRecordKeys.get(index), Number.class);

          Object temp = targetList.get(inRecordKeys.get(index));
          if (temp instanceof LinkedList) {
            value = (LinkedList<Number>) temp;
          } else {
            value = new LinkedList<Number>();
            LOGGER.info("Instantiate new Linked List for : {}", inRecordKeys.get(index));
          }

          if (value != null) {
            if (value.size() >= this.mWindowSize) {
              value.removeFirst();
            }
            value.addLast(predict);
            targetList.put(inRecordKeys.get(index), value);

            Double[] tArr = value.toArray(new Double[0]);
            Double[] tObs = observationList.toArray(new Double[0]);

            in.setValue(outRecordKeys.get(index), getAverageError(tArr, tObs));
          }
        }
      } else {
        LOGGER.error("Failed to extract Observation value {} ", this.observation);
      }
    } else {
      ArrayList<Number> observe = in.getValue(this.observation, ArrayList.class);
      if (observe != null) {
        for (int index = 0; index < inRecordKeys.size(); index++) {
          ArrayList<Number> predict = in.getValue(inRecordKeys.get(index), ArrayList.class);

          if (predict != null && observe.size() == predict.size()) {
            Double[] tArr = observe.toArray(new Double[0]);
            Double[] tObs = predict.toArray(new Double[0]);

            in.setValue(outRecordKeys.get(index), getAverageError(tArr, tObs));
          } else {
            LOGGER.error("Size of List not match Observe {} : Predict {}", observe.size(),
                (predict != null ? predict.size() : -1));
          }
        }
      }
    }
    LOGGER.info("[Error] Returning calculation result");
    return in;
  }
}
