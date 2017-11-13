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
package com.sec.processing.framework.task.function;

public final class CommonFunction {

    private CommonFunction() {  }

    /**
     * Override function
     * Multiply values in arrays
     * @param x double []
     * @param y double []
     * @return
     */
    public static double product(double[] x, double[] y) {
        double ret = 0.0;

        if (x.length != y.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        for (int iter = 0; iter < x.length; iter++) {
            ret += x[iter] * y[iter];
        }

        return ret;
    }

    /**
     * Override function
     * Multiply values in arrays
     * @param x Double []
     * @param y Double []
     * @return
     */
    public static double product(Double[] x, Double[] y) {
        double ret = 0.0;

        if (x.length != y.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        for (int iter = 0; iter < x.length; iter++) {
            ret += x[iter].doubleValue() * y[iter].doubleValue();
        }

        return ret;
    }

    /**
     * Override function
     * Multiply values in arrays
     * @param x Double []
     * @param y double []
     * @return
     */
    public static double product(Double[] x, double[] y) {
        double ret = 0.0;

        if (x.length != y.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        for (int iter = 0; iter < x.length; iter++) {
            ret += x[iter].doubleValue() * y[iter];
        }

        return ret;
    }

    /**
     * Override function
     * Multiply values until it has its pair
     * @param x double []
     * @param y double []
     * @return
     */
    public static double forceProduct(double[] x, double[] y) {
        double ret = 0.0;

        if (x.length <= y.length) {

            for (int iter = 0; iter < x.length; iter++) {
                ret += x[iter] * y[iter];
            }
        } else {
            for (int iter = 0; iter < y.length; iter++) {
                ret += x[iter] * y[iter];
            }
        }

        return ret;
    }

    /**
     * Override function
     * Multiply values until it has its pair
     * @param x Double []
     * @param y double []
     * @return
     */
    public static double forceProduct(Double[] x, double[] y) {
        double ret = 0.0;

        if (x.length <= y.length) {

            for (int iter = 0; iter < x.length; iter++) {
                ret += x[iter].doubleValue() * y[iter];
            }
        } else {
            for (int iter = 0; iter < y.length; iter++) {
                ret += x[iter].doubleValue() * y[iter];
            }
        }

        return ret;
    }

    /**
     * Override function
     * Multiply values until it has its pair
     * @param x Double []
     * @param y Double []
     * @return
     */
    public static double forceProduct(Double[] x, Double[] y) {
        double ret = 0.0;

        if (x.length <= y.length) {

            for (int iter = 0; iter < x.length; iter++) {
                ret += x[iter].doubleValue() * y[iter].doubleValue();
            }
        } else {
            for (int iter = 0; iter < y.length; iter++) {
                ret += x[iter].doubleValue() * y[iter].doubleValue();
            }
        }

        return ret;
    }

    /**
     * Arithmetic additions
     * @param op1
     * @param op2
     * @return
     * @throws Exception
     */
    public static Object add(Object op1, Object op2) throws Exception {

//        if( op1 instanceof String || op2 instanceof String){
//            return String.valueOf(op1) + String.valueOf(op2);
//        }

        if( !(op1 instanceof Number) || !(op2 instanceof Number) ){
            throw new Exception("invalid operands for mathematical operator [+]");
        }

        if(op1 instanceof Double || op2 instanceof Double){
            return ((Number)op1).doubleValue() + ((Number)op2).doubleValue();
        }

        if(op1 instanceof Float || op2 instanceof Float){
            return ((Number)op1).floatValue() + ((Number)op2).floatValue();
        }

        if(op1 instanceof Long || op2 instanceof Long){
            return ((Number)op1).longValue() + ((Number)op2).longValue();
        }

        return ((Number)op1).intValue() + ((Number)op2).intValue();
    }
    /**
     * Arithmetic subtraction
     * @param op1
     * @param op2
     * @return
     * @throws Exception
     */
    public static Object sub(Object op1, Object op2) throws Exception {

        if( !(op1 instanceof Number) || !(op2 instanceof Number) ){
            throw new Exception("invalid operands for mathematical operator [-]");
        }

        if(op1 instanceof Double || op2 instanceof Double){
            return ((Number)op1).doubleValue() - ((Number)op2).doubleValue();
        }

        if(op1 instanceof Float || op2 instanceof Float){
            return ((Number)op1).floatValue() - ((Number)op2).floatValue();
        }

        if(op1 instanceof Long || op2 instanceof Long){
            return ((Number)op1).longValue() - ((Number)op2).longValue();
        }

        return ((Number)op1).intValue() - ((Number)op2).intValue();
    }
    /**
     * Arithmetic multiplacation
     * @param op1
     * @param op2
     * @return
     * @throws Exception
     */
    public static Object mul(Object op1, Object op2) throws Exception {

        if( !(op1 instanceof Number) || !(op2 instanceof Number) ){
            throw new Exception("invalid operands for mathematical operator [-]");
        }

        if(op1 instanceof Double || op2 instanceof Double){
            return ((Number)op1).doubleValue() * ((Number)op2).doubleValue();
        }

        if(op1 instanceof Float || op2 instanceof Float){
            return ((Number)op1).floatValue() * ((Number)op2).floatValue();
        }

        if(op1 instanceof Long || op2 instanceof Long){
            return ((Number)op1).longValue() * ((Number)op2).longValue();
        }

        return ((Number)op1).intValue() * ((Number)op2).intValue();
    }
    /**
     * Arithmetic divition
     * @param op1
     * @param op2
     * @return
     * @throws Exception
     */
    public static Object div(Object op1, Object op2) throws Exception {

        if( !(op1 instanceof Number) || !(op2 instanceof Number) ){
            throw new Exception("invalid operands for mathematical operator [-]");
        }

        if(op1 instanceof Double || op2 instanceof Double){
            return ((Number)op1).doubleValue() / ((Number)op2).doubleValue();
        }

        if(op1 instanceof Float || op2 instanceof Float){
            return ((Number)op1).floatValue() / ((Number)op2).floatValue();
        }

        if(op1 instanceof Long || op2 instanceof Long){
            return ((Number)op1).longValue() / ((Number)op2).longValue();
        }

        return ((Number)op1).intValue() / ((Number)op2).intValue();
    }
    /**
     * Arithmetic modulus
     * @param op1
     * @param op2
     * @return
     * @throws Exception
     */
    public static Object mod(Object op1, Object op2) throws Exception {

        if( !(op1 instanceof Number) || !(op2 instanceof Number) ){
            throw new Exception("invalid operands for mathematical operator [-]");
        }

        if(op1 instanceof Double || op2 instanceof Double){
            return ((Number)op1).doubleValue() % ((Number)op2).doubleValue();
        }

        if(op1 instanceof Float || op2 instanceof Float){
            return ((Number)op1).floatValue() % ((Number)op2).floatValue();
        }

        if(op1 instanceof Long || op2 instanceof Long){
            return ((Number)op1).longValue() % ((Number)op2).longValue();
        }

        return ((Number)op1).intValue() % ((Number)op2).intValue();
    }
//
//    /**
//     * Get number of factorial with given value
//     * @param num
//     * @return factorial value
//     */
//    public static double factorial(double num) {
//        double fact = 1;
//        for (int i=2;i<=num; i++){
//            fact=fact*i;
//        }
//        return fact;
//    }
//
//    /**
//     * Get number of all the combination cases
//     * @param num Total number of given cases
//     * @param k Number of cases to be included
//     * @return number of all possibles cases
//     */
//    public static double combination(double num, double k) {
//        if (num > 0.0 && k > 0.0) {
//            if ( (k == 1) || (num == k) ){
//                return 1.0;
//            } else if( num > k )  {
//                double denominator = factorial(num);
//                double numerator = (factorial(k) * factorial((num - k)));
//
//                return (denominator / numerator);
//            } else {
//                return -1.0;
//            }
//        }  else {
//            return -1.0;
//        }
//    }
}
