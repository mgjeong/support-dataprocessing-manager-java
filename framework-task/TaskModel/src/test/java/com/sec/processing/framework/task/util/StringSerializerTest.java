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
package com.sec.processing.framework.task.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

public class StringSerializerTest {
    @Test
    public void testSerializableObject() throws IOException, ClassNotFoundException {
        SimpleObject ross = SimpleObject.create("Ross Geller", 30, 80.3, true);
        SimpleObject rachel = SimpleObject.create("Rachel Green", 28, 56.4, false);

        // encode
        String strRoss = StringSerializer.encode(ross);
        String strRachel = StringSerializer.encode(rachel);

        Assert.assertNotNull(strRoss);
        Assert.assertNotNull(strRachel);

        System.out.println("Ross: " + strRoss);
        System.out.println("Rachel: " + strRachel);

        // decode
        SimpleObject newRoss = StringSerializer.decode(strRoss, SimpleObject.class);
        SimpleObject newRachel = StringSerializer.decode(strRachel, SimpleObject.class);

        Assert.assertNotNull(newRoss);
        Assert.assertNotNull(newRachel);

        Assert.assertTrue(ross.equals(newRoss));
        Assert.assertTrue(rachel.equals(newRachel));
    }

    @Test
    public void testBase64() {
        String randomUUID = UUID.randomUUID().toString();
        String encoded = StringSerializer.encodeBase64(randomUUID);
        Assert.assertNotNull(encoded);
        String decoded = StringSerializer.decodeBase64(encoded);
        Assert.assertEquals(randomUUID, decoded);
    }

    private static class SimpleObject implements Serializable {
        String name;
        int age;
        double weight;
        boolean valid;

        private SimpleObject(String name, int age, double weight, boolean valid) {
            this.name = name;
            this.age = age;
            this.weight = weight;
            this.valid = valid;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SimpleObject)) {
                return false;
            } else if (obj == this) {
                return true;
            }

            SimpleObject other = (SimpleObject) obj;

            return this.name.equals(other.name) && this.age == other.age
                    && this.weight == other.weight && this.valid == other.valid;
        }

        public static SimpleObject create(String name, int age, double weight, boolean valid) {
            return new SimpleObject(name, age, weight, valid);
        }
    }
}
