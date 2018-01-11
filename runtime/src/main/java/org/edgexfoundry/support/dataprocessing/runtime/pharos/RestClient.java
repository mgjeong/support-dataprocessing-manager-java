package org.edgexfoundry.support.dataprocessing.runtime.pharos;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.edgexfoundry.support.dataprocessing.runtime.connection.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RestClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestClient.class);

    public static final String GROUP_API_BASE = "/api/v1/groups";
    public static final String AGENT_API_BASE = "/api/v1/agents";
    public static final String APPS_URL = "/apps";

    private HTTP httpClient;
    private Gson gson;

    public RestClient() {
        httpClient = new HTTP();
        httpClient.initialize(PharosConstants.PHAROS_HOST, PharosConstants.PHAROS_PORT, "http");
        gson = new Gson();
    }

    public List<String> getGroupList() {
        LOGGER.debug("Get groups from Pharos / URI: {}", GROUP_API_BASE);

        List<String> groupList = new ArrayList<String>();

        JsonObject response = httpGet(GROUP_API_BASE);
        JsonArray groups = response.getAsJsonArray(PharosConstants.PHAROS_JSON_SCHEMA_GROUPS);
        Iterator<JsonElement> iter = groups.iterator();

        while (iter.hasNext()) {
            JsonObject group = iter.next().getAsJsonObject();
            String id = group.get(PharosConstants.PHAROS_JSON_SCHEMA_GROUP_ID).getAsString();

            groupList.add(id);
        }

        return groupList;
    }

    public List<String> getEdgeIdList(String groupId) {
        LOGGER.debug("Get edge id list from Pharos / URI: {}", GROUP_API_BASE + "/" + groupId);

        List<String> edgeList = new ArrayList<String>();

        JsonObject response = httpGet(GROUP_API_BASE + "/" + groupId);
        JsonArray members = response.getAsJsonArray(PharosConstants.PHAROS_JSON_SCHEMA_GROUP_MEMBERS);
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

        edgeInfo.put("id", edgeId);
        edgeInfo.put("host", response.get(PharosConstants.PHAROS_JSON_SCHEMA_HOST_NAME).getAsString());

        JsonArray apps = response.getAsJsonArray(PharosConstants.PHAROS_JSON_SCHEMA_APPS);
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
        LOGGER.debug("Get app information from Pharos / URI: {}", AGENT_API_BASE + "/" + edgeId + APPS_URL + "/" + appId);

        List<String> serviceList = new ArrayList<String>();

        JsonObject response = httpGet(AGENT_API_BASE + "/" + edgeId + APPS_URL + "/" + appId);
        JsonArray services = response.getAsJsonArray(PharosConstants.PHAROS_JSON_SCHEMA_SERVICES);

        Iterator<JsonElement> iter = services.iterator();

        while (iter.hasNext()) {
            JsonObject tmp = iter.next().getAsJsonObject();

            if (tmp.getAsJsonObject(PharosConstants.PHAROS_JSON_SCHEMA_APP_STATE)
                    .get(PharosConstants.PHAROS_JSON_SCHEMA_APP_STATE_STATUS).getAsString()
                    .equals(PharosConstants.PHAROS_JSON_SCHEMA_APP_STATE_RUNNING)) {
                String name = tmp.get(PharosConstants.PHAROS_JSON_SCHEMA_APP_NAME).getAsString();

                serviceList.add(name);
            }

        }

        return serviceList;
    }

    private JsonObject httpGet(String url) {
        JsonElement jsonElem = this.httpClient.get(url);
        JsonObject jsonResponse = jsonElem.getAsJsonObject();

        return jsonResponse;
    }
}
