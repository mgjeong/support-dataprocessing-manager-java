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

import java.io.UnsupportedEncodingException;

public final class ZmqUtil {

  private static final String LOSSLESS_CHARSET = "ISO-8859-1";

  private ZmqUtil() {
    throw new UnsupportedOperationException("No instance of this class is allowed.");
  }

  public static String encode(byte[] b) {
    return encode(b, LOSSLESS_CHARSET);
  }

  private static String encode(byte[] b, String charset) {
    try {
      return new String(b, charset);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] decode(String s) {
    return decode(s, LOSSLESS_CHARSET);
  }

  private static byte[] decode(String s, String charset) {
    try {
      return s.getBytes(charset);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
