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
package org.edgexfoundry.support.dataprocessing.runtime.task.function;

public final class KernelFunction {

    private KernelFunction() {  }

    public enum KERNELTYPE { GAUSSIAN, LOGISTIC, SIGMOID, INVALID  }

    private static double gaussian(double value) {

        double exponant = ((-1.0) / 2.0) * (Math.pow(value, 2));
        double front = (1.0 / Math.sqrt(2.0 * Math.PI));

        return (front * Math.exp(exponant));
    }

    private static double logistic(double value) {
        return (1.0 / (Math.exp(value) + 2 + Math.exp((-1.0) * value)));
    }

    private static double sigmod(double value) {
        return (2.0 / Math.sqrt(Math.PI)) * (1.0 / (Math.exp(value) + Math.exp((-1.0) * value)));
    }

    /**
     * Calculated value with kernel function
     * @param value
     * @param type Kernel function type (GAUSSIAN, LOGISTIC, SIGMOID)
     * @return Calculated value
     */
    public static double calculate(double value, KERNELTYPE type) {
        if (type == KERNELTYPE.GAUSSIAN) {

            return gaussian(value);

        } else if (type == KERNELTYPE.LOGISTIC) {

            return logistic(value);

        } else if (type == KERNELTYPE.SIGMOID) {

            return sigmod(value);

        } else {

            System.err.println("NOT SUPPORTED TYPE");

            return 0;
        }
    }
}
