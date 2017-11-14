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
package org.edgexfoundry.processing.runtime.task.function;

public final class DistanceFunction {

    private DistanceFunction () {    }

    /**
     * Euclidian distance
     * @param first
     * @param second
     * @return
     */
    public static Double distEuclidean (Double[] first, Double[] second) {
        // calculate absolute relative distance
        Double distance = 0.0;

        if(first.length != second.length) {
            System.err.println("Invalid Parameter length First ("+first.length+") Second ("+second.length+")");
        } else {
            for (int i = 0; i < first.length; i++) {
                distance += Math.pow(first[i] - second[i], 2);
            }
            distance = Math.sqrt(distance);
        }
        return distance;
    }

    /**
     * Abs_relative distance
     * @param first
     * @param second
     * @param max
     * @param min
     * @return
     */
    public static Double distABSRelative (Double[] first, Double[] second, Double[] max, Double[] min) {
        // calculate absolute relative distance
        Double distance = 0.0;

        if(first.length != second.length) {

            System.err.println("Invalid Parameter length First ("+first.length+") Second ("+second.length+")");

        } else {

            for (int i = 0; i < first.length; i++) {
                distance += Math.abs(first[i] - second[i]) / (max[i] - min[i]);
            }

            distance = Math.sqrt(distance);
        }

        return distance;
    }

    /**
     * Mahalanobis distance
     * @param first
     * @param second
     * @param covariance : Pre-calculated standard deviation values avg(distance between centroid & point)
     * @return
     */
    public static Double distMahalanobis (Double[] first, Double[] second, Double[] covariance ) {
        // calculate absolute relative distance
        Double distance = 0.0;
        Double z;

        if(first.length != second.length) {

            System.err.println("Invalid Parameter length First ("+first.length+") Second ("+second.length+")");

        } else {

            for (int i = 0; i < first.length; i++) {
                z = ( first[i] - second[i] ) / covariance[i];
                distance += Math.pow( z, 2 );
            }

            distance = Math.sqrt(distance);
        }

        return distance;
    }
}
