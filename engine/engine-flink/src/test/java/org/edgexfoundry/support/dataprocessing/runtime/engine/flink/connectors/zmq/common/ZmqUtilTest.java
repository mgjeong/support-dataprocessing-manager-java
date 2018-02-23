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
package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.zmq.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.Assert;
import org.junit.Test;

public class ZmqUtilTest {

  @Test
  public void testPrivateConstructor() throws Exception {
    Constructor<ZmqUtil> c = ZmqUtil.class.getDeclaredConstructor();
    c.setAccessible(true);

    try {
      c.newInstance();
    } catch (InvocationTargetException e) {
      Assert.assertTrue(e.getTargetException() instanceof UnsupportedOperationException);
      return;
    }
    Assert.fail("Should not reach here.");
  }

  @Test
  public void testDecode() {
    String testString = "Hello World!";
    byte[] decoded = ZmqUtil.decode(testString);

    Assert.assertNotNull(decoded);
    Assert.assertTrue(decoded.length > 0);
  }

  @Test
  public void testDecodeInvalid() throws Exception {
    String testString = "Hello World!";

    Method m = ZmqUtil.class.getDeclaredMethod("decode", String.class, String.class);
    m.setAccessible(true);
    try {
      m.invoke(null, testString, "InvalidCharset");
    } catch (InvocationTargetException e) {
      Assert.assertTrue(e.getTargetException() instanceof RuntimeException);
      return;
    }
    Assert.fail("Should not reach here.");
  }

  @Test
  public void testEncode() {
    String testString = "Hello World!";
    byte[] decoded = ZmqUtil.decode(testString);

    Assert.assertNotNull(decoded);
    Assert.assertTrue(decoded.length > 0);

    String encoded = ZmqUtil.encode(decoded);

    Assert.assertNotNull(encoded);
    Assert.assertEquals(testString, encoded);
  }

  @Test
  public void testEncodingInvalid() throws Exception {
    String testString = "Hello World!";
    byte[] decoded = ZmqUtil.decode(testString);

    Assert.assertNotNull(decoded);
    Assert.assertTrue(decoded.length > 0);

    Method m = ZmqUtil.class.getDeclaredMethod("encode", byte[].class, String.class);
    m.setAccessible(true);
    try {
      m.invoke(null, decoded, "InvalidCharset");
    } catch (InvocationTargetException e) {
      Assert.assertTrue(e.getTargetException() instanceof RuntimeException);
      return;
    }
    Assert.fail("Should not reach here.");
  }
}
