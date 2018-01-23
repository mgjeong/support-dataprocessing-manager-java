package org.edgexfoundry.support.dataprocessing.runtime.pharos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgeInfo {

  private static final Logger LOGGER = LoggerFactory.getLogger(EdgeInfo.class);

  private RestClient restClient;

  public EdgeInfo() {
    restClient = new RestClient();
  }

  public List<Map<String, String>> getGroupList() {
    List<String> groupIdList = restClient.getGroupList();
    Iterator<String> iter = groupIdList.iterator();

    List<Map<String, String>> groupList = new ArrayList<Map<String, String>>();

    // TODO : When pharos provide group name, this loop will be removed
    while (iter.hasNext()) {
      Map<String, String> map = new HashMap<String, String>();
      String id = iter.next();

      map.put("id", id);
      map.put("name", id);

      groupList.add(map);
    }

    // TODO: temporary: add localhost as default group, if group does not exist
    if (groupList.isEmpty()) {
      Map<String, String> group = new HashMap<>();
      group.put("id", "Local");
      group.put("name", "Local");

      groupList.add(group);
    }

    return groupList;
  }

  public List<String> getEngineList(String groupId, String engineType) {
    List<String> edgeIdList;
    try {
      edgeIdList = restClient.getEdgeIdList(groupId);
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      edgeIdList = new ArrayList<>();
    }

    Iterator<String> iter = edgeIdList.iterator();

    List<String> engineList = new ArrayList<String>();

    while (iter.hasNext()) {
      String edgeId = iter.next();
      Map<String, ?> edgeInfo = restClient.getEdgeInfo(edgeId);

      if (edgeInfo == null) {
        continue;
      }

      List<String> apps = (List<String>) edgeInfo.get("apps");
      Iterator<String> appIter = apps.iterator();

      while (appIter.hasNext()) {
        String appId = appIter.next();

        List<String> services = restClient.getServiceList(edgeId, appId);
        Iterator<String> serviceIter = services.iterator();

        while (serviceIter.hasNext()) {
          String service = serviceIter.next();

          if (engineType.equals("ANY") &&
              (service.equals(PharosConstants.FLINK_NAME) || service
                  .equals(PharosConstants.KAPACITOR_NAME))) {
            engineList.add((String) edgeInfo.get("host"));
            break;
          } else if (engineType.equals("FLINK") && service.equals(PharosConstants.FLINK_NAME)) {
            String flinkAddress = (String) edgeInfo.get("host");
            engineList.add(flinkAddress + ":" + PharosConstants.FLINK_PORT);
            break;
          } else if (engineType.equals("KAPACITOR") && service
              .equals(PharosConstants.KAPACITOR_NAME)) {
            String kapacitorAddress = (String) edgeInfo.get("host");
            engineList.add(kapacitorAddress + ":" + PharosConstants.KAPACITOR_PORT);
            break;
          }
        }
      }
    }

    // TODO: temporary, add localhost for debugging/testing purpose
    if (engineType.equalsIgnoreCase("FLINK")) {
      engineList.add("localhost:8081");
    } else {
      engineList.add("localhost:9092");
    }

    return engineList;
  }
}
