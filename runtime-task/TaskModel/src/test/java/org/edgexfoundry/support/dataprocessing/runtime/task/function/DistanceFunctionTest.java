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

public class DistanceFunctionTest {

  @Test
  public void testDistEuclidean() {

    Double[] first = {1.0, 2.0, 3.0};
    Double[] second = {3.0, 4.0, 5.0};

    Double dist = DistanceFunction.distEuclidean(first, second);

    Assert.assertEquals(3.4641016151377544, dist, 0);
  }

  @Test
  public void testDistABSRelative() {

    Double[] first = {1.0, 2.0, 3.0};
    Double[] second = {2.0, 4.0, 6.0};

    Double[] min = {1.0, 1.0, 1.0};
    Double[] max = {3.0, 3.0, 3.0};

    Double dist = DistanceFunction.distABSRelative(first, second, max, min);

    Assert.assertEquals(1.7320508075688772, dist, 0);
  }


  @Test
  public void testDistMahalanobis() {

    Double[] first = {1.0, 2.0, 3.0};
    Double[] second = {2.0, 4.0, 6.0};

    Double[] covariance = {1.0, 1.0, 1.0};

    Double dist = DistanceFunction.distMahalanobis(first, second, covariance);

    Assert.assertEquals(3.7416573867739413, dist, 0);
  }
}
