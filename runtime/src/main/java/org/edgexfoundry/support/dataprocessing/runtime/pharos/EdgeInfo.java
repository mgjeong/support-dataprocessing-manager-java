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

    return groupList;
  }

  public List<String> getEngineList(String groupId, String engineType) {
    List<String> edgeIdList = restClient.getEdgeIdList(groupId);
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
          } else if (engineType.equals("KAPACITOR") && service.equals(PharosConstants.KAPACITOR_NAME)) {
            String kapacitorAddress = (String) edgeInfo.get("host");
            engineList.add(kapacitorAddress + ":" + PharosConstants.KAPACITOR_PORT);
            break;
          }
        }
      }
    }

    return engineList;
  }
}
