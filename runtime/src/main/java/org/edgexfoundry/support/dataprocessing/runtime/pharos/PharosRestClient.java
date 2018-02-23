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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.edgexfoundry.support.dataprocessing.runtime.connection.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PharosRestClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(PharosRestClient.class);

  public static final String GROUP_API_BASE = "/api/v1/groups";
  public static final String AGENT_API_BASE = "/api/v1/agents";
  public static final String APPS_URL = "/apps";

  private HTTP httpClient;

  public PharosRestClient() {
    httpClient = new HTTP();
    httpClient.initialize(PharosConstants.PHAROS_HOST, PharosConstants.PHAROS_PORT, "http");
  }

  public List<String> getGroupList() {
    LOGGER.debug("Get groups from Pharos / URI: {}", GROUP_API_BASE);

    List<String> groupList = new ArrayList<String>();

    JsonObject response = httpGet(GROUP_API_BASE);

    if (response == null) {
      return groupList;
    }

    JsonArray groups = response.getAsJsonArray(PharosConstants.PHAROS_JSON_SCHEMA_GROUPS);
    if (groups == null) {
      LOGGER.warn("group list is empty");
      return groupList;
    }

    Iterator<JsonElement> iter = groups.iterator();

    while (iter.hasNext()) {
      JsonObject group = iter.next().getAsJsonObject();
      JsonElement groupId = group.get(PharosConstants.PHAROS_JSON_SCHEMA_GROUP_ID);
      if(groupId != null) {
        String id = groupId.getAsString();
        groupList.add(id);
      } else {
        LOGGER.warn("groupId is null");
      }
    }

    return groupList;
  }

  public List<String> getEdgeIdList(String groupId) {
    LOGGER.debug("Get edge id list from Pharos / URI: {}", GROUP_API_BASE + "/" + groupId);

    List<String> edgeList = new ArrayList<String>();

    JsonObject response = httpGet(GROUP_API_BASE + "/" + groupId);

    if (response == null) {
      return edgeList;
    }

    JsonArray members = response.getAsJsonArray(PharosConstants.PHAROS_JSON_SCHEMA_GROUP_MEMBERS);

    if (members == null) {
      return edgeList;
    }

    Iterator<JsonElement> iter = members.iterator();

    while (iter.hasNext()) {
      String id = iter.next().getAsString();

      edgeList.add(id);
    }

    return edgeList;
  }

  public Map<String, ?> getEdgeInfo(String edgeId) {
    LOGGER.debug("Get edge information from Pharos / URI: {}", AGENT_API_BASE + "/" + edgeId);

    Map<String, Object> edgeInfo = new HashMap<String, Object>();

    JsonObject response = httpGet(AGENT_API_BASE + "/" + edgeId);

    if (response == null) {
      return null;
    }


    edgeInfo.put("id", edgeId);

    JsonElement hostname = response.get(PharosConstants.PHAROS_JSON_SCHEMA_HOST_NAME);
    if(hostname != null) {
      edgeInfo.put("host", hostname.getAsString());
    } else {
      LOGGER.warn("host name is null");
    }

    JsonArray apps = response.getAsJsonArray(PharosConstants.PHAROS_JSON_SCHEMA_APPS);
    if(apps == null) {
      LOGGER.warn("app list is empty");
      return null;
    }

    Iterator<JsonElement> iter = apps.iterator();

    List<String> appIdList = new ArrayList<String>();

    while (iter.hasNext()) {
      String id = iter.next().getAsString();

      appIdList.add(id);
    }

    edgeInfo.put("apps", appIdList);

    return edgeInfo;
  }

  public List<String> getServiceList(String edgeId, String appId) {
    LOGGER.debug("Get app information from Pharos / URI: {}",
        AGENT_API_BASE + "/" + edgeId + APPS_URL + "/" + appId);

    List<String> serviceList = new ArrayList<String>();

    JsonObject response = httpGet(AGENT_API_BASE + "/" + edgeId + APPS_URL + "/" + appId);

    if (response == null) {
      return serviceList;
    }

    JsonArray services = response.getAsJsonArray(PharosConstants.PHAROS_JSON_SCHEMA_SERVICES);

    if(services == null){
      LOGGER.warn("service list is empty");
      return serviceList;
    }

    Iterator<JsonElement> iter = services.iterator();

    while (iter.hasNext()) {
      JsonObject tmp = iter.next().getAsJsonObject();
      JsonObject appState = tmp.getAsJsonObject(PharosConstants.PHAROS_JSON_SCHEMA_APP_STATE);
      JsonElement appStateStatus = null;
      if(appState != null) {
        appStateStatus = appState.get(PharosConstants.PHAROS_JSON_SCHEMA_APP_STATE_STATUS);
      }

      if (appStateStatus != null && appStateStatus.getAsString()
          .equals(PharosConstants.PHAROS_JSON_SCHEMA_APP_STATE_RUNNING)) {
        JsonElement appName = tmp.get(PharosConstants.PHAROS_JSON_SCHEMA_APP_NAME);
        if(appName != null) {
          String name = appName.getAsString();
          serviceList.add(name);
        } else {
          LOGGER.warn("app name is null");
        }
      }

    }

    return serviceList;
  }

  private JsonObject httpGet(String url) {
    JsonElement jsonElem = this.httpClient.get(url);

    if (jsonElem == null) {
      return null;
    } else {
      return jsonElem.getAsJsonObject();
    }
  }
}
