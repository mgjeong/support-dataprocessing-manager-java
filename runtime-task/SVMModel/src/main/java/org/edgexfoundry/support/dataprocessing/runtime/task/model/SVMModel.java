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

import org.edgexfoundry.support.dataprocessing.runtime.task.AbstractTaskModel;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModelParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskParam.UiFieldType;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.edgexfoundry.support.dataprocessing.runtime.task.model.libsvm.svm;
import org.edgexfoundry.support.dataprocessing.runtime.task.model.libsvm.svm_model;
import org.edgexfoundry.support.dataprocessing.runtime.task.model.libsvm.svm_node;
import org.edgexfoundry.support.dataprocessing.runtime.task.model.libsvm.svm_parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SVMModel extends AbstractTaskModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(SVMModel.class);

  private double[][] mSupportVectors = null;
  private svm_model model = null;

  @TaskParam(key = "labels", uiName = "Label for each classes", uiType = UiFieldType.ARRAYNUMBER, tooltip = "Enter Labels of each classes")
  private String[] mClasses = null;

  @TaskParam(key = "svmType", uiName = "SVM type", uiType = UiFieldType.NUMBER, tooltip = "C_SVC(0), NU_SVC(1), ONE_CLASS(2), EPSILON_SVR(3), NU_SVR(4)")
  protected Integer algoType; // model.param.svm_type

  @TaskParam(key = "kernelType", uiName = "Kernel type", uiType = UiFieldType.STRING, tooltip = "linear, polynomial, radial, sigmoid")
  protected String kernelType; // model.param.kernel_type

  @TaskParam(key = "degree", uiName = "Degree Value", uiType = UiFieldType.NUMBER, tooltip = "Enter degree value")
  protected Double degree; // model.param.degree

  @TaskParam(key = "gamma", uiName = "Gamma Value", uiType = UiFieldType.NUMBER, tooltip = "Enter gamma value")
  protected Double gamma; // model.param.gamma

  @TaskParam(key = "coef0", uiName = "Coefficient0 Value", uiType = UiFieldType.NUMBER, tooltip = "Enter coef0 value")
  protected Double coef0; // model.param.coef0

  @TaskParam(key = "nSV", uiName = "# of supporting vectors for each classes", uiType = UiFieldType.ARRAYNUMBER, tooltip = "Enter # of S.Vectors")
  protected List<Number> nSV; // model.nSV

  @TaskParam(key = "sVectors", uiName = "Lists of supporting vectors for each classes", uiType = UiFieldType.ARRAYOBJECT, tooltip = "Enter list of  S.Vectors")
  protected List<List<Number>> sVectors; // model.SV

  @TaskParam(key = "svCoef", uiName = "Lists of coefficients of supporting vectors of each classes", uiType = UiFieldType.ARRAYOBJECT, tooltip = "Enter coefficients of S.Vectors")
  protected List<List<Number>> scCoef; // model.sv_coef

  @TaskParam(key = "rho", uiName = "rho", uiType = UiFieldType.ARRAYNUMBER, tooltip = "Enter RHO values for each classes")
  protected List<Number> rho; // model.rho

  public SVMModel() {
    model = new svm_model();
    model.param = new svm_parameter();
    model.SV = null;
    model.rho = null;
    model.probA = null;
    model.probB = null;
    model.label = null;
    model.nSV = null;
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
    return "svm";
  }

//    /**
//     * Override function
//     * Get default parameter values for the reference
//     */
//    public TaskModelParam getDefaultParam() {
//        double[][] arrSupportVectors = { {1.1, 2.2, 3.3}, {1.1, 2.2, 3.3}, {1.1, 2.2, 3.3} };
//        double[][] trainedCoef = {
//                {1.1037055147699631E-4, 1.1037055147699631E-4, -1.1037055147699631E-4, -1.1037055147699631E-4}
//        };
//        int[] nr_sv = {4, 4};
//        double[] rho = {1.7957345715960713, 1.7957345715960713};
//        String[] arrNames = {"a", "b"};
//
//        ArrayList<ArrayList<Double>> sVectorList = new ArrayList();
//        for (int index = 0; index < arrSupportVectors.length; index++) {
//            ArrayList<Double> tList = new ArrayList<>();
//            for (int index2 = 0; index2 < arrSupportVectors[index].length; index2++) {
//                tList.add(arrSupportVectors[index][index2]);
//            }
//            sVectorList.add(tList);
//        }
//        ArrayList<ArrayList<Double>> coefList = new ArrayList();
//        for (int index = 0; index < trainedCoef.length; index++) {
//            ArrayList<Double> tList = new ArrayList<>();
//            for (int index2 = 0; index2 < trainedCoef[index].length; index2++) {
//                tList.add(trainedCoef[index][index2]);
//            }
//            coefList.add(tList);
//        }
//        ArrayList<Double> rhoList = new ArrayList<>();
//        for (int index2 = 0; index2 < rho.length; index2++) {
//            rhoList.add(rho[index2]);
//        }
//        ArrayList<Integer> nSVList = new ArrayList<>();
//        for (int index2 = 0; index2 < nr_sv.length; index2++) {
//            nSVList.add(nr_sv[index2]);
//        }
//        List<String> clsNames = Arrays.asList(arrNames);
//
//        TaskModelParam params = new TaskModelParam();
//        TaskModelParam clsInfo = new TaskModelParam();
//        TaskModelParam kernel = new TaskModelParam();
//
//        kernel.put("type", "sigmoid");
//        kernel.put("gamma", 0.25);
//        kernel.put("coef0", 0.0);
//
//        clsInfo.put("labels", clsNames);
//        clsInfo.put("nSV", nSVList);
//        clsInfo.put("rho", rhoList);
//        clsInfo.put("sVectors", sVectorList);
//        clsInfo.put("svCoef", coefList);
//
//        params.put("type", 1);
//        params.put("kernel", kernel);
//        params.put("classInfo", clsInfo);
//
//        return params;
//    }

  @Override
  /**
   * Override function
   * Set parameter values which are required for processing this task
   */
  public void setParam(TaskModelParam params) {
    LOGGER.info("[SVM] Entering setParam Method");
    try {
      // C_SVC(0), NU_SVC(1), ONE_CLASS(2), EPSILON_SVR(3), NU_SVR(4)
      model.param.svm_type = Integer.parseInt(params.get("svmType").toString());

      if (params.containsKey("kernelType")) {
        String type = (String) params.get("kernelType");
        if (type.equals("linear")) {
          this.model.param.kernel_type = 0; // (u * v)
        } else if (type.equals("polynomial")) {
          this.model.param.kernel_type = 1; // (gamma * u * v + coef) ^ degree
        } else if (type.equals("radial")) {
          this.model.param.kernel_type = 2; // exp(-gamma * |u -v|^2
        } else if (type.equals("sigmoid")) {
          this.model.param.kernel_type = 3; //tanh(gamma * u * v + coef)
        } else {
          throw new IllegalArgumentException();
        }
      }
      if (params.containsKey("degree")) {
        model.param.degree = Integer.parseInt(params.get("degree").toString());
      }
      if (params.containsKey("gamma")) {
        model.param.gamma = Double.parseDouble(params.get("gamma").toString());
      }
      if (params.containsKey("coef0")) {
        model.param.coef0 = Double.parseDouble(params.get("coef0").toString());
      }
      if (params.containsKey("labels")) {
        List<String> lList = (List<String>) params.get("labels");
        mClasses = lList.toArray(new String[lList.size()]);
        // Set number of classes
        model.nr_class = mClasses.length;
        // Set label of classes (using numeric value by default)
        model.label = new int[mClasses.length];
        for (int index = 0; index < model.label.length; index++) {
          model.label[index] = index;
        }
      }
      if (params.containsKey("nSV")) {
        // Set number of SVs for each classes.
        model.nSV = TaskModelParam
            .transformToNativeInt1DArray((List<Number>) params.get("nSV"));
      }
      if (params.containsKey("rho")) {
        model.rho = TaskModelParam
            .transformToNativeDouble1DArray((List<Number>) params.get("rho"));
      }
      if (params.containsKey("sVectors")) {
        mSupportVectors = TaskModelParam
            .transformToNativeDouble2DArray((List<List<Number>>) params.get("sVectors"));
        // Set number of features.
        model.l = mSupportVectors.length;
        // Set support vectors for each classes
        model.SV = new svm_node[model.l][];

        if (mSupportVectors.length == model.l) {
          for (int i = 0; i < model.l; i++) {
            model.SV[i] = new svm_node[mSupportVectors[0].length];
            for (int j = 0; j < mSupportVectors[0].length; j++) {
              model.SV[i][j] = new svm_node();
              model.SV[i][j].index = j + 1;
              model.SV[i][j].value = mSupportVectors[i][j];
            }
          }
        }
      }
      if (params.containsKey("svCoef")) {
        model.sv_coef = TaskModelParam
            .transformToNativeDouble2DArray((List<List<Number>>) params.get("svCoef"));
      }
    } catch (IllegalArgumentException e) {
      LOGGER.error("[SVM] " + e.toString());
    } catch (NullPointerException e) {
      LOGGER.error("[SVM] " + e.toString());
    }

    LOGGER.info("[SVM] Leaving setParam Method");
  }

  @Override
  public DataSet calculate(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {
    LOGGER.info("[SVM] String calculation");
    List<List<Number>> vectors = new ArrayList<>();
    for (int index = 0; index < inRecordKeys.size(); index++) {
      List<Number> values = in.getValue(inRecordKeys.get(index), List.class);
      vectors.add(values);
    }
    if (vectors.size() == inRecordKeys.size()) {
      for (int loop = 0; loop < vectors.get(0).size(); loop++) {
        svm_node[] dataRecord = new svm_node[inRecordKeys.size()];
        for (int index = 0; index < inRecordKeys.size(); index++) {
          dataRecord[index] = new svm_node();
          dataRecord[index].index = index + 1;
          dataRecord[index].value = vectors.get(index).get(loop).doubleValue();
        }

        double result = svm.svm_predict(model, dataRecord);

        in.setValue(outRecordKeys.get(0), mClasses[(int) Math.floor(result)]);
        //System.out.println("MSG : " + in.toString());
      }
    } else {
      LOGGER.error("[SVM] Feature value extraction from given data failed~!!");
    }

    LOGGER.info("[SVM] Returning calculation result");
    return in;
  }
}