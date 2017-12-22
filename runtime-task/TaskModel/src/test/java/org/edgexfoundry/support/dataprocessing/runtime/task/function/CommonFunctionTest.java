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

    @Test
    public void ArithmeticAddTest() {

        Number x = 1;
        Number y = -1;

        Number z = null;
        try {

            z =  (Number)CommonFunction.add(x.intValue(),y.intValue());

            Assert.assertEquals(z.intValue(), 0,0);

            z =  (Number)CommonFunction.add(x.longValue(),y.longValue());

            Assert.assertEquals(z.longValue(), 0,0);

            z =  (Number)CommonFunction.add(x.floatValue(),y.floatValue());

            Assert.assertEquals(z.floatValue(), 0.0,0);

            z =  (Number)CommonFunction.add(x.doubleValue(),y.doubleValue());

            Assert.assertEquals(z.doubleValue(), 0.0,0);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void ArithmeticSubTest() {

        Number x = 1;
        Number y = -1;

        Number z = null;
        try {

            z =  (Number)CommonFunction.sub(x.intValue(),y.intValue());

            Assert.assertEquals(z.intValue(), 2,0);

            z =  (Number)CommonFunction.sub(x.longValue(),y.longValue());

            Assert.assertEquals(z.longValue(), 2,0);

            z =  (Number)CommonFunction.sub(x.floatValue(),y.floatValue());

            Assert.assertEquals(z.floatValue(), 2.0,0);

            z =  (Number)CommonFunction.sub(x.doubleValue(),y.doubleValue());

            Assert.assertEquals(z.doubleValue(), 2.0,0);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void ArithmeticMulTest() {

        Number x = 1;
        Number y = -1;

        Number z = null;
        try {

            z =  (Number)CommonFunction.mul(x.intValue(),y.intValue());

            Assert.assertEquals(z.intValue(), -1,0);

            z =  (Number)CommonFunction.mul(x.longValue(),y.longValue());

            Assert.assertEquals(z.longValue(), -1,0);

            z =  (Number)CommonFunction.mul(x.floatValue(),y.floatValue());

            Assert.assertEquals(z.floatValue(), -1.0,0);

            z =  (Number)CommonFunction.mul(x.doubleValue(),y.doubleValue());

            Assert.assertEquals(z.doubleValue(), -1.0,0);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void ArithmeticDivTest() {

        Number x = 1;
        Number y = -1;

        Number z = null;
        try {

            z =  (Number)CommonFunction.div(x.intValue(),y.intValue());

            Assert.assertEquals(z.intValue(), -1,0);

            z =  (Number)CommonFunction.div(x.longValue(),y.longValue());

            Assert.assertEquals(z.longValue(), -1,0);

            z =  (Number)CommonFunction.div(x.floatValue(),y.floatValue());

            Assert.assertEquals(z.floatValue(), -1.0,0);

            z =  (Number)CommonFunction.div(x.doubleValue(),y.doubleValue());

            Assert.assertEquals(z.doubleValue(), -1.0,0);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void ArithmeticModest() {

        Number x = 2;
        Number y = 1;

        Number z = null;
        try {

            z =  (Number)CommonFunction.mod(x.intValue(),y.intValue());

            Assert.assertEquals(z.intValue(), 0,0);

            z =  (Number)CommonFunction.mod(x.longValue(),y.longValue());

            Assert.assertEquals(z.longValue(), 0,0);

            z =  (Number)CommonFunction.mod(x.floatValue(),y.floatValue());

            Assert.assertEquals(z.floatValue(), 0.0,0);

            z =  (Number)CommonFunction.mod(x.doubleValue(),y.doubleValue());

            Assert.assertEquals(z.doubleValue(), 0.0,0);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
