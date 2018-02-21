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
package org.edgexfoundry.support.dataprocessing.runtime.pharos;

public final class PharosConstants {

  private PharosConstants() {
  }

  public static final String PHAROS_HOST = "10.113.66.234";
  public static final Integer PHAROS_PORT = 48099;

  public static final String PHAROS_JSON_SCHEMA_GROUPS = "groups";
  public static final String PHAROS_JSON_SCHEMA_GROUP_ID = "id";
  public static final String PHAROS_JSON_SCHEMA_GROUP_MEMBERS = "members";

  public static final String PHAROS_JSON_SCHEMA_AGENTS = "agents";
  public static final String PHAROS_JSON_SCHEMA_AGENT_ID = "id";
  public static final String PHAROS_JSON_SCHEMA_HOST_NAME = "host";
  public static final String PHAROS_JSON_SCHEMA_APPS = "apps";
  public static final String PHAROS_JSON_SCHEMA_SERVICES = "services";
  public static final String PHAROS_JSON_SCHEMA_APP_NAME = "name";
  public static final String PHAROS_JSON_SCHEMA_APP_STATE = "state";
  public static final String PHAROS_JSON_SCHEMA_APP_STATE_STATUS = "Status";
  public static final String PHAROS_JSON_SCHEMA_APP_STATE_RUNNING = "running";

  public static final String FLINK_NAME = "flink";
  public static final Integer FLINK_PORT = 8081;
  public static final String KAPACITOR_NAME = "kapacitor-engine";
  public static final Integer KAPACITOR_PORT = 9092;
}
