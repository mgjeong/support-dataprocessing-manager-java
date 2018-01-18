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

public final class Settings {

  public static final String DOCKER_PATH = "/runtime/ha/";
  public static final String FW_JAR_PATH = DOCKER_PATH + "jar/task/";
  public static final String CUSTOM_JAR_PATH = DOCKER_PATH + "jar/task_user/";
  public static final String RESOURCE_PATH = DOCKER_PATH + "resource/";

  public static final String DB_PATH = "DPFW.db";
  public static final String DB_TEST_PATH = "DPFW_TEST.db";
  public static final String DB_CLASS = "org.sqlite.JDBC";

  public static final String WEB_ADDRESS = "localhost";
  public static final int WEB_PORT = 8082;
  public static final String WEB_SCHEMA = "http";


  private Settings() {

  }
}
