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

package org.edgexfoundry.support.dataprocessing.runtime.util;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JarLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(JarLoader.class);

  public static <T> T newInstance(File jarFile, String className, ClassLoader classLoader,
      Class<T> clazz)
      throws Exception {
    if (jarFile == null || !jarFile.exists()) {
      throw new RuntimeException("Invalid jar file");
    } else if (StringUtils.isEmpty(className)) {
      throw new RuntimeException("Invalid classname for " + jarFile.getName());
    }

    try (URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()},
        classLoader)) {
      Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      method.setAccessible(true);
      method.invoke(urlClassLoader, new Object[]{jarFile.toURI().toURL()});

      Class classToInstantiate = Class.forName(className, true, urlClassLoader);
      if (Modifier.isAbstract(classToInstantiate.getModifiers())) {
        LOGGER.error(className + " is an abstract class.");
        return null;
      } else {
        Object o = classToInstantiate.newInstance();
        if (clazz.isInstance(o)) {
          return clazz.cast(o);
        } else {
          LOGGER.error(className + " is not an instance of " + clazz.getCanonicalName());
          return null;
        }
      }
    }
  }
}
