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

import org.junit.Assert;
import org.junit.Test;

public class CommonFunctionTest {

    @Test
    public void productTest() {

        double[] x = {1, 2, 3};

        double[] y = {1, 2, 3};
        double[] y2 = {1, 2};

        double z = CommonFunction.product(x, y);

        Assert.assertEquals(z, 14, 0);

        try {

            z = CommonFunction.product(x, y2);

        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println();
        }
    }

    @Test
    public void product2Test() {

        Double[] x = {1.0, 2.0, 3.0};

        Double[] y = {1.0, 2.0, 3.0};

        Double[] y2 = {1.0, 2.0};

        double z = CommonFunction.product(x, y);

        Assert.assertEquals(z, 14, 0);

        try {

            z = CommonFunction.product(x, y2);

        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println();
        }
    }

    @Test
    public void product3Test() {

        Double[] x = {1.0, 2.0, 3.0};

        double[] y = {1.0, 2.0, 3.0};
        double[] y2 = {1, 2};

        double z = CommonFunction.product(x, y);

        Assert.assertEquals(z, 14, 0);

        try {

            z = CommonFunction.product(x, y2);

        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println();
        }
    }

    @Test
    public void forceProductTest() {

        double[] x = {1.0, 2.0, 3.0};

        double[] y = {1.0, 2.0};

        double z = CommonFunction.forceProduct(x, y);

        Assert.assertEquals(z, 5, 0);


        z = CommonFunction.forceProduct(y, x);

        Assert.assertEquals(z, 5, 0);

    }

    @Test
    public void forceProduct2Test() {

        Double[] x1 = {1.0, 2.0, 3.0};

        double[] y1 = {1.0, 2.0};

        Double[] x2 = {1.0, 2.0};

        double[] y2 = {1.0, 2.0, 3.0};

        double z = CommonFunction.forceProduct(x1, y1);

        Assert.assertEquals(z, 5, 0);


        z = CommonFunction.forceProduct(x2, y2);

        Assert.assertEquals(z, 5, 0);

    }


    @Test
    public void forceProduct3Test() {

        Double[] x1 = {1.0, 2.0, 3.0};

        Double[] y1 = {1.0, 2.0};

        Double[] x2 = {1.0, 2.0};

        Double[] y2 = {1.0, 2.0, 3.0};

        double z = CommonFunction.forceProduct(x1, y1);

        Assert.assertEquals(z, 5, 0);


        z = CommonFunction.forceProduct(x2, y2);

        Assert.assertEquals(z, 5, 0);

    }
}
