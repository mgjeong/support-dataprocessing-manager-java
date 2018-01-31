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
package org.edgexfoundry.support.dataprocessing.runtime;

import java.io.File;
import org.edgexfoundry.support.dataprocessing.runtime.engine.MonitoringManager;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

  private static void initialize() throws Exception {

    // 1. Check if resource directory exists, make and copy resources if necessary
    makeResourceDirectoryIfNecessary();

    // 2. Check if database exists, run bootstrap if necessary
    makeDatabaseIfNecessary();

    // 3. Run task manager to scan for tasks
    TaskManager.getInstance().initialize(Settings.CUSTOM_JAR_PATH);
    TaskManager.getInstance().scanTaskModel(Settings.FW_JAR_PATH);

    // 4. Run Monitoring
    MonitoringManager.getInstance().setInterval(MonitoringManager.INTERVAL).start();
  }

  private static void makeDatabaseIfNecessary() throws Exception {
    // Check database
    File db = new File(Settings.DOCKER_PATH + Settings.DB_PATH);
    if (!db.exists()) {
      LOGGER.info("Executing bootstrap on {}", db.getAbsolutePath());
      Bootstrap bootstrap = new Bootstrap();
      bootstrap.execute();
    }
  }

  private static void makeResourceDirectoryIfNecessary() throws Exception {
    // Check jar directory
    File fwJarPath = new File(Settings.FW_JAR_PATH);
    makeDirectory(fwJarPath);

    // Check custom jar directory
    File customJarPath = new File(Settings.CUSTOM_JAR_PATH);
    makeDirectory(customJarPath);

    // Check resource directory
    File resourcePath = new File(Settings.RESOURCE_PATH);
    makeDirectory(resourcePath);

    // Copy resources
    // TODO: copy engine-flink from resource?
  }

  private static void makeDirectory(File dirPath) throws Exception {
    if (!dirPath.exists()) {
      boolean success = dirPath.mkdirs();
      if (!success) {
        throw new Exception("Failed to create " + dirPath.getAbsolutePath());
      }
    } else if (!dirPath.isDirectory()) {
      throw new Exception(dirPath.getAbsolutePath() + " is not a directory.");
    }
  }

  private static void terminate() {
    TaskManager.getInstance().terminate();

    try {
      MonitoringManager.getInstance().stop();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws Exception {
    initialize();

    SpringApplication.run(Application.class, args);

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        terminate();
      }
    });
  }
}
