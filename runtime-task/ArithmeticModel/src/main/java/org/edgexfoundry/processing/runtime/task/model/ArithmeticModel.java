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
import org.edgexfoundry.processing.runtime.task.function.CommonFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Public class which provide arithmetic operation functionality.
 */
public class ArithmeticModel extends AbstractTaskModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArithmeticModel.class);

    private static final String OPERATIONS = "operations";
    private static final String OPERAND_LEFT = "leftOperand";
    private static final String OPERAND_RIGHT = "rightOperand";
    private static final String OPERATOR = "operator";
    private static final String OPERATOR_ADD = "add"; /* or +  */
    private static final String OPERATOR_SUB = "sub"; /* or -  */
    private static final String OPERATOR_MUL = "mul"; /* or *  */
    private static final String OPERATOR_DIV = "div"; /* or /  */
    private static final String OPERATOR_MOD = "mod"; /* or %  */
    private static final String TYPE = "type";
    private static final String TYPE_SINGLE = "single";
    private static final String TYPE_ARRAY = "array";
    private static final String RESULT = "outputKey";

    private List<HashMap<String, Object>> mOperations = null;

    @Override
    /**
     * Override function
     * Get type of this task
     */
    public TaskType getType() {
        return TaskType.PREPROCESSING;
    }

    @Override
    /**
     * Override function
     * Get name of this task
     */
    public String getName() {
        return "arithmetic";
    }

    @Override
    /**
     * Override function
     * Get default parameter values for the reference
     */
    public TaskModelParam getDefaultParam() {

        /*
        * ex) "operations" : [
        *         {   // KEY_A * KEY_B
        *             "leftOperand": "KEY_A",
        *             "rightOperand": "KEY_B",
        *             "operator" : "mul",
        *             "type" : "array",
        *             "outputKey":"mul"
        *         }
        * ]
        */
        TaskModelParam op2 = new TaskModelParam();
        op2.put(OPERAND_LEFT, "/records/BC");
        op2.put(OPERAND_RIGHT, "/records/BL");
        op2.put(OPERATOR, OPERATOR_MUL);
        op2.put(TYPE, TYPE_ARRAY);
        op2.put(RESULT, "/records/*/"+OPERATOR_MUL);
        TaskModelParam op3 = new TaskModelParam();
        op3.put(OPERAND_LEFT, "/records/BC");
        op3.put(OPERAND_RIGHT, "/records/BL");
        op3.put(OPERATOR, OPERATOR_ADD);
        op3.put(TYPE, TYPE_ARRAY);
        op3.put(RESULT, "/records/*/"+OPERATOR_ADD);


        List<TaskModelParam> operations = new ArrayList<>();
        operations.add(op2);
        operations.add(op3);

        TaskModelParam params = new TaskModelParam();
        params.put(OPERATIONS, operations);

        return params;
    }

    @Override
    /**
     * Override function
     * Set parameter values which are required for processing this task
     */
    public void setParam(TaskModelParam params) {

        if (params.containsKey("operations")) {
            mOperations = (List<HashMap<String, Object>>) params.get("operations");
        }
    }

    private Object operation(Object left, Object right, String operator)
            throws Exception {
        if (operator.equals(OPERATOR_ADD) || operator.equals("+")) {
            return CommonFunction.add(left, right);
        } else if (operator.equals(OPERATOR_SUB) || operator.equals("-")) {
            return CommonFunction.sub(left, right);
        } else if (operator.equals(OPERATOR_MUL) || operator.equals("*")) {
            return CommonFunction.mul(left, right);
        } else if (operator.equals(OPERATOR_DIV) || operator.equals("/")) {
            return CommonFunction.div(left, right);
        } else if (operator.equals(OPERATOR_MOD) || operator.equals("%")) {
            return CommonFunction.mod(left, right);
        } else {
            //System.out.println("Hello : " +operator);
            LOGGER.error("Invalid Operator type : " + operator);
            return null;
        }
    }

    private DataSet doOperations(DataSet in) {

        try {
//            System.out.println("size : "+this.mOperations.size());
            for (HashMap<String, Object> operation : this.mOperations) {

                String lOperand = (String) operation.get(OPERAND_LEFT);
                String rOperand = (String) operation.get(OPERAND_RIGHT);
                String operator = (String) operation.get(OPERATOR);
                String type = (String) operation.get(TYPE);
                String resultKey = (String) operation.get(RESULT);

                // The result will always be array type..
                List<Object> result = new ArrayList<>();

                if (type.equals(TYPE_SINGLE)) {
                    // If operation between two single variables
                    Object lKeyValue = in.getValue(lOperand, Object.class);
                    Object rKeyValue = in.getValue(rOperand, Object.class);

                    Object res = operation(lKeyValue, rKeyValue, operator);
                    result.add(res);

                    if (result.size() > 0) {
                        in.setValue(resultKey, res);
                    }

                } else if (type.equals(TYPE_ARRAY)) {
                    // If operation between two arrays
                    // note that the array size of those two arrays should be equal
                    List<Object> lOpKeyValues = in.getValue(lOperand, List.class);
                    List<Object> rOpKeyValues = null;

                    Object rOpKeyValue = in.getValue(rOperand, Object.class);
                    if (rOpKeyValue instanceof Number) {
                        rOpKeyValues = new ArrayList<>();
                        rOpKeyValues.add(rOpKeyValue);
                    } else if (rOpKeyValue instanceof List) {
                        rOpKeyValues = (List<Object>) (rOpKeyValue);
                    }

                    if (lOpKeyValues.size() < 1 || rOpKeyValues.size() < 1) {
                        //System.err.println("Operand array length should not be zero");
                        LOGGER.error("Operand array length should not be zero");
                        return in;
                    }

                    // DO NOTHING, if the size of the operand array are no equal
                    if (lOpKeyValues.size() == rOpKeyValues.size()) {
                        // If it's M by M matrix operation..
                        for (int index = 0; index < lOpKeyValues.size(); index++) {
                            Object res = operation(lOpKeyValues.get(index),
                                    rOpKeyValues.get(index),
                                    operator);

                            result.add(res);
                        }
                    } else if (rOpKeyValues.size() == 1) {
                        // If it's M by 1 matrix operation..
                        for (int index = 0; index < lOpKeyValues.size(); index++) {
                            Object res = operation(lOpKeyValues.get(index),
                                    rOpKeyValues.get(0),
                                    operator);

                            result.add(res);
                        }
                    } else {
                        //System.err.println("Operand array length is different ");
                        LOGGER.error("Operand array length is different");
                        return in;
                    }

                    if (result.size() > 0) {
                        in.setValue(resultKey, result);
                    }
                }
            }
        } catch (Exception e) {
            //System.err.println("[ERROR] ARITHMETIC OPERATION : "+e.getMessage());
            LOGGER.error("[ERROR] ARITHMETIC OPERATION : " + e.getMessage());
        } finally {
            // TO guarantee the integrity of input data, returning received data set
            return in;
        }
    }

    @Override
    /**
     * Override function which calculate error value
     */
    public DataSet calculate(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {


        in = doOperations(in);

        return in;
    }
}
