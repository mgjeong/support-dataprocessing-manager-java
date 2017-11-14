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
package org.edgexfoundry.support.dataprocessing.runtime.task.function;

public class SigmoidFunction {
    private SigmoidFunction() {   }

    public enum TYPESIGMOID { LOGISTIC, TANH,  INVALID  }

    private static double logistic(double value) {
        return (1 / (1 + Math.exp(-1 * value)));
    }

    private static double hyperbolicTan(double value) {
        return Math.tanh(value);
    }

    /**
     * Calculate sigmoid value
     * @param value
     * @param type SIGMOID Function type (LOGISTIC, TANH)
     * @return calculated value
     */
    public static double calculate(double value, TYPESIGMOID type) {
        if (type == TYPESIGMOID.LOGISTIC) {
            return logistic(value);
        } else if (type == TYPESIGMOID.TANH) {
            return hyperbolicTan(value);
        } else {
            System.err.println("NOT SUPPORTED TYPE");
            return 0;
        }
    }
}
