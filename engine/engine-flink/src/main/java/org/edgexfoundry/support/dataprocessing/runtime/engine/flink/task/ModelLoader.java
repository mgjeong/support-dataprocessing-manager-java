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
package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.task;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModelLoader.class);
  private URLClassLoader urlClassLoader = null;
  private ClassLoader classLoader = null;
  private String jarPath = null;

  public ModelLoader(String jarPath, ClassLoader classLoader) throws Exception {
    if (null == jarPath) {
      throw new NullPointerException("Jar path should be specified");
    }

    this.jarPath = jarPath;
    this.classLoader = classLoader;
  }

  public TaskModel newInstance(String modelName) throws Exception {
    loadJar(jarPath);
    Class<TaskModel> cls = getClassInstance(modelName);

    if (Modifier.isAbstract(cls.getModifiers())) {
      // if abstract class, we cannot instantiate
      return null;
    } else {
      Object obj = cls.newInstance();
      return (TaskModel) obj;
    }
  }

  private Class getClassInstance(String className)
      throws ClassNotFoundException, NoClassDefFoundError {
    return urlClassLoader.loadClass(className);
  }

  /*
    Jar file for Target TaskModel exists inside the root of Flink job jar file.
    In other words, jar:file://[TASK MODEL NAME].jar
   */
  private void loadJar(String jarPath) throws Exception {
    String inJarPath = "/" + jarPath;
    File targetJar = new File(jarPath);
    LOGGER.info("HH {}", targetJar.getAbsolutePath());
    if (!targetJar.exists()) {
      LOGGER.info("HH {}", getClass().getResource("/" + jarPath));
      InputStream jarStream = getClass().getResourceAsStream(inJarPath);
      Files.copy(jarStream, targetJar.getAbsoluteFile().toPath());
    }

    this.urlClassLoader = new URLClassLoader(new URL[]{targetJar.toURI().toURL()},
        this.classLoader);

    Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
    method.setAccessible(true);
    method.invoke(this.classLoader, new Object[]{targetJar.toURI().toURL()});

    LOGGER.info("URL ClassLoader loaded: " + jarPath);
  }

}
