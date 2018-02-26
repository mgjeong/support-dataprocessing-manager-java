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
package org.edgexfoundry.support.dataprocessing.runtime;

public final class Settings {

  public static final String DOCKER_PATH = "/runtime/ha/";
  public static final String FW_JAR_PATH = DOCKER_PATH + "jar/task/";
  public static final String CUSTOM_JAR_PATH = DOCKER_PATH + "jar/task_user/";
  public static final String RESOURCE_PATH = DOCKER_PATH + "resource/";

  public static final String DB_PATH = "DPFW.db";
  public static final String JDBC_PATH = "jdbc:sqlite:" + DOCKER_PATH + DB_PATH;

  public static final String API_MAX_FILE_SIZE = "16MB";
  public static final String API_MAX_REQUEST_SIZE = "32MB";

  public static final long JOB_MONITORING_INTERVAL = 5 * 1000L; // in milliseconds

  private static Settings instance = null;

  public synchronized static Settings getInstance() {
    if (instance == null) {
      instance = new Settings();
    }
    return instance;
  }

  private Settings() {

  }

  public String getDockerPath() {
    return DOCKER_PATH;
  }

  public String getFwJarPath() {
    return FW_JAR_PATH;
  }

  public String getCustomJarPath() {
    return CUSTOM_JAR_PATH;
  }

  public String getResourcePath() {
    return RESOURCE_PATH;
  }

  public String getDbPath() {
    return DB_PATH;
  }

  public String getJdbcPath() {
    return JDBC_PATH;
  }

  public String getApiMaxFileSize() {
    return API_MAX_FILE_SIZE;
  }

  public String getApiMaxRequestSize() {
    return API_MAX_REQUEST_SIZE;
  }

  public long getJobMonitoringInterval() {
    return JOB_MONITORING_INTERVAL;
  }
}
