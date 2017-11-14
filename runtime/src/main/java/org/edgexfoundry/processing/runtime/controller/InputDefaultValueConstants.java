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

package com.sec.processing.framework.controller;

public class InputDefaultValueConstants {
    private static final String LOGISTIC_BAD_WEIGHT
            = "{\"jobs\":[{\"input\":[{\"dataType\":\"ZMQ\",\"dataSource\":\"localhost:5555:topic\"}],\"output\":[{\"dataType\":\"F\",\"dataSource\":\"output\"}],\"task\":[{\"type\":\"REGRESSION\",\"name\":\"LogisticRegression\",\"params\":{\"error\":\"1\",\"weights\":\"1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1\"}}],\"state\":\"CREATE\"}]}";
    private static final String LOGISTIC_GOOD_WEIGHT
            = "{\"jobs\":[{\"input\":[{\"dataType\":\"ZMQ\",\"dataSource\":\"localhost:5555:topic\"}],\"output\":[{\"dataType\":\"F\",\"dataSource\":\"output\"}],\"task\":[{\"type\":\"REGRESSION\",\"name\":\"LogisticRegression\",\"params\":{\"error\":\"-1.704e16\",\"weights\":\"-4.508e15 -2.681e13 5.592e14 6.374e12 4.804e16 -2.931e16 3.110e14 8.171e15 -8.564e15 -1.030e16 8.161e15 -4.617e14 -8.989e14 6.591e12 -1.037e16 4.729e16 -2.689e16 1.993e17 -4.806e16 -4.364e17 1.102e15 1.390e14 6.421e13 -9.950e12 -6.379e15 -7.493e15 6.282e15 -7.308e15 1.098e16 4.755e16\"}}],\"state\":\"CREATE\"}]}";
    private static final String CSVPARSER_LOGISTIC_GOOD_WEIGHT
            = "{\"jobs\":[{\"input\":[{\"dataType\":\"ZMQ\",\"dataSource\":\"localhost:5555:topic\"}],\"output\":[{\"dataType\":\"F\",\"dataSource\":\"output\"}],\"task\":[{\"type\":\"PREPROCESSING\",\"name\":\"CsvParser\",\"params\":{\"delimiter\":\"\\t\",\"index\":\"1 2 3 5 6 7 9 10 11 13 14 15 17 18 19 21 22 23 25 26 27 29 30 31 33 34 35 37 38 39\"}},{\"type\":\"REGRESSION\",\"name\":\"LogisticRegression\",\"params\":{\"error\":\"-1.704e16\",\"weights\":\"-4.508e15 -2.681e13 5.592e14 6.374e12 4.804e16 -2.931e16 3.110e14 8.171e15 -8.564e15 -1.030e16 8.161e15 -4.617e14 -8.989e14 6.591e12 -1.037e16 4.729e16 -2.689e16 1.993e17 -4.806e16 -4.364e17 1.102e15 1.390e14 6.421e13 -9.950e12 -6.379e15 -7.493e15 6.282e15 -7.308e15 1.098e16 4.755e16\"}}],\"state\":\"CREATE\"}]}";
   private static final String SIMPLE_MOVING_AVERAGE
            = "{\"jobs\":[{\"input\":[{\"dataType\":\"ZMQ\",\"dataSource\":\"localhost:5555:topic\"}],\"output\":[{\"dataType\":\"WS\",\"dataSource\":\"localhost:8083\"},{\"dataType\":\"F\",\"dataSource\":\"output\"}],\"task\":[{\"type\":\"TREND\",\"name\":\"sma\",\"params\":{\"interval\":{\"data\":3}}},{\"type\":\"PREPROCESSING\",\"name\":\"JsonGenerator\",\"params\":{\"keys\":\"Result \"}}],\"state\":\"CREATE\"}]}";
   private static final String COMPLEX_MOVING_AVERAGE
            = "{\"jobs\":[{\"input\":[{\"dataType\":\"ZMQ\",\"dataSource\":\"10.113.64.225:5555:topic\"}],\"output\":[{\"dataType\":\"ZMQ\",\"dataSource\":\"127.0.0.1:5556:topic\"}],\"task\":[{\"type\":\"TREND\",\"name\":\"sma\",\"params\":{\"interval\":{\"data\":1}}},{\"type\":\"PREPROCESSING\",\"name\":\"JsonGenerator\",\"params\":{\"keys\":\"1\"}}],\"state\":\"CREATE\"},{\"input\":[{\"dataType\":\"ZMQ\",\"dataSource\":\"10.113.64.225:5555:topic\"}],\"output\":[{\"dataType\":\"ZMQ\",\"dataSource\":\"127.0.0.1:5557:topic\"}],\"task\":[{\"type\":\"TREND\",\"name\":\"sma\",\"params\":{\"interval\":{\"data\":10}}},{\"type\":\"PREPROCESSING\",\"name\":\"JsonGenerator\",\"params\":{\"keys\":\"10\"}}],\"state\":\"CREATE\"},{\"input\":[{\"dataType\":\"ZMQ\",\"dataSource\":\"10.113.64.225:5555:topic\"}],\"output\":[{\"dataType\":\"ZMQ\",\"dataSource\":\"127.0.0.1:5558:topic\"}],\"task\":[{\"type\":\"TREND\",\"name\":\"sma\",\"params\":{\"interval\":{\"data\":20}}},{\"type\":\"PREPROCESSING\",\"name\":\"JsonGenerator\",\"params\":{\"keys\":\"20\"}}],\"state\":\"CREATE\"},{\"input\":[{\"dataType\":\"ZMQ\",\"dataSource\":\"127.0.0.1:5556:topic\"},{\"dataType\":\"ZMQ\",\"dataSource\":\"127.0.0.1:5557:topic\"},{\"dataType\":\"ZMQ\",\"dataSource\":\"127.0.0.1:5558:topic\"}],\"output\":[{\"dataType\":\"WS\",\"dataSource\":\"10.113.64.225:8083\"}],\"task\":[{\"type\":\"PREPROCESSING\",\"name\":\"Aggregator\",\"params\":{\"keys\":\"1 10 20\",\"aggregateBy\":\"Id\"}},{\"type\":\"PREPROCESSING\",\"name\":\"JsonGenerator\",\"params\":{\"keys\":\"1 10 20\"}}],\"state\":\"CREATE\"}]}";

    public static final String CREATEJOB_DEFAULT_VALUE
            = COMPLEX_MOVING_AVERAGE;
    public static final String UPDATEJOB_DEFAULT_VALUE
            = LOGISTIC_GOOD_WEIGHT;

    protected InputDefaultValueConstants() {

    }

}
