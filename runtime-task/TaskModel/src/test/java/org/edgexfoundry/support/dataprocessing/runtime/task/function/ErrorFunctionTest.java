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

import org.junit.Assert;
import org.junit.Test;

public class ErrorFunctionTest {

  @Test
  public void calculateTest() {
    double[] prediction = {1.0, 2.0, 3.0};
    double[] observation = {1.1, 2.1, 2.9};

    double[] predictionErr = {1.0, 2.0, 3.0};
    double[] observationErr = {1.1, 2.1, 3.1};

    double error = 0.0;

    error = ErrorFunction.calculate(prediction, observation, ErrorFunction.MEASURE.INVALID);

    Assert.assertEquals(error, -1, 0);

    error = ErrorFunction.calculate(prediction, observation, ErrorFunction.MEASURE.MSE);

    Assert.assertEquals(error, 0.010000000000000018, 0);

    error = ErrorFunction.calculate(prediction, observation, ErrorFunction.MEASURE.RMSE);

    Assert.assertEquals(error, 0.10000000000000009, 0);

    error = ErrorFunction.calculate(prediction, observationErr, ErrorFunction.MEASURE.ME);

    Assert.assertEquals(error, 0.10000000000000009, 0);

    error = ErrorFunction.calculate(predictionErr, observation, ErrorFunction.MEASURE.MAE);

    Assert.assertEquals(error, 0.10000000000000009, 0);
  }
}
