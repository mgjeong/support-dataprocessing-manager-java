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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class JarLoader {
  private static final Logger LOGGER = LoggerFactory.getLogger(JarLoader.class);

  private URLClassLoader urlClassLoader = null;
  private ClassLoader classLoader = null;

  public JarLoader(String jarPath, ClassLoader classLoader) throws Exception {
    if (null == jarPath) {
      return;
    }

    this.classLoader = classLoader;

    loadJar(jarPath);
  }

  /**
   * @param className This should be included the package name.
   *                  likes "org.edgexfoundry.support.dataprocessing.runtime.task.model.SVMModel"
   * @return
   */
  public Class getClassInstance(String className) throws ClassNotFoundException, NoClassDefFoundError {

    LOGGER.info("Attempting to load " + className);
    return urlClassLoader.loadClass(className);

  }

  public void loadJar(String jarPath) throws Exception {

    File file = new File(jarPath);

    if (!file.exists() || file.length() == 0) {
      throw new RuntimeException("Jar file is not valid. " + jarPath);
    }
    //urlClassLoader = new URLClassLoader(new URL[] {file.toURL()}, ClassLoader.getSystemClassLoader());
    //this.urlClassLoader = new URLClassLoader(new URL[] {file.toURI().toURL()}, this.getClass().getClassLoader());
    this.urlClassLoader = new URLClassLoader(new URL[]{file.toURI().toURL()}, this.classLoader);

    Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
    method.setAccessible(true);
    method.invoke(this.classLoader, new Object[]{file.toURI().toURL()});

    LOGGER.info("URL ClassLoader loaded: " + jarPath);
  }

  public void unloadJar() throws IOException {
    urlClassLoader.close();
  }

}
