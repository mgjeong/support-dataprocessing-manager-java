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
package org.edgexfoundry.processing.runtime.task.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Base64;

public final class StringSerializer {
    private static final String DEFAULT_CHARSET = "UTF-8";

    public static String decodeBase64(String s) {
        return new String(Base64.getDecoder().decode(s.getBytes(Charset.forName(DEFAULT_CHARSET))));
    }

    public static String encodeBase64(String s) {
        return new String(Base64.getEncoder().encode(s.getBytes(Charset.forName(DEFAULT_CHARSET))));
    }

    public static <T> T decode(String s, Class<T> clazz) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(s);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new ByteArrayInputStream(data));
            Object o = ois.readObject();
            return clazz.cast(o);
        } finally {
            if (ois != null) {
                ois.close();
            }
        }
    }

    public static String encode(Serializable o) throws IOException {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;

        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } finally {
            if (oos != null) {
                oos.close();
            }
            if (baos != null) {
                baos.close();
            }
        }
    }
}
