package org.edgexfoundry.support.dataprocessing.runtime.pharos;

import static org.edgexfoundry.support.dataprocessing.runtime.pharos.PharosRestClient.AGENT_API_BASE;
import static org.edgexfoundry.support.dataprocessing.runtime.pharos.PharosRestClient.APPS_URL;
import static org.edgexfoundry.support.dataprocessing.runtime.pharos.PharosRestClient.GROUP_API_BASE;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import com.google.gson.JsonParser;
import java.util.List;
import java.util.Map;
import org.edgexfoundry.support.dataprocessing.runtime.connection.HTTP;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PharosRestClient.class)
public class EdgeInfoTest {

  private static EdgeInfo edgeInfo;
  private static HTTP mockedHttp;

  @BeforeClass
  public static void initialize() throws Exception{
    mockedHttp = mock(HTTP.class);
    whenNew(HTTP.class).withNoArguments().thenReturn(mockedHttp);
    when(mockedHttp.initialize(PharosConstants.PHAROS_HOST, PharosConstants.PHAROS_PORT, "http"))
        .thenReturn(null);

    edgeInfo = new EdgeInfo();
  }

  @Test
  public void getGroupListTest() {
    String testJsonString = "{\n"
        + "\"groups\": \n"
        + "[\n"
        + " {\n"
        + "   \"id\":\"id1111\"\n"
        + " },\n"
        + " {\n"
        + "   \"id\":\"id2222\"\n"
        + " },\n"
        + " {\n"
        + "   \"id\":\"id3333\"\n"
        + " }\n"
        + "]\n"
        + "}\n";
    JsonParser parser = new JsonParser();
    when(mockedHttp.get(GROUP_API_BASE)).thenReturn(parser.parse(testJsonString));

    List<Map<String, String>> groupList = edgeInfo.getGroupList();

    Assert.assertTrue(groupList.get(0).get("id").equals("id1111"));
    Assert.assertTrue(groupList.get(0).get("name").equals("id1111"));
    Assert.assertTrue(groupList.get(1).get("id").equals("id2222"));
    Assert.assertTrue(groupList.get(1).get("name").equals("id2222"));
    Assert.assertTrue(groupList.get(2).get("id").equals("id3333"));
    Assert.assertTrue(groupList.get(2).get("name").equals("id3333"));
  }

  @Test
  public void getEngineListTest() {
    String edgeListString = "{\n"
        + "\"id\":\"id1111\",\n"
        + "\"members\": [\"edge1\", \"edge2\", \"edge3\"]\n"
        + "}";

    String edgeInfo1String = "{\n"
        + "\"id\":\"edge1\",\n"
        + "\"host\":\"192.168.0.1\",\n"
        + "\"port\":8080,\n"
        + "\"apps\" : [\"app1\"]\n"
        + "}";

    String edgeInfo2String = "{\n"
        + "\"id\":\"edge2\",\n"
        + "\"host\":\"192.168.0.1\",\n"
        + "\"port\":8080,\n"
        + "\"apps\" : [\"app2\"]\n"
        + "}";

    String edgeInfo3String = "{\n"
        + "\"id\":\"edge3\",\n"
        + "\"host\":\"192.168.0.1\",\n"
        + "\"port\":8080,\n"
        + "\"apps\" : [\"app3\"]\n"
        + "}";

    String serviceList1String = "{\n"
        + "\"services\": \n"
        + "[\n"
        + "{\n"
        + "\"name\":\"kapacitor-engine\",\n"
        + "\"state\":\n"
        + "{\n"
        + "\"ExitCode\":\"0\",\n"
        + "\"Status\":\"running\"\n"
        + "}\n"
        + "},\n"
        + "{\n"
        + "\"name\":\"flink\",\n"
        + "\"state\":\n"
        + "{\n"
        + "\"ExitCode\":\"0\",\n"
        + "\"Status\":\"exited\"\n"
        + "}\n"
        + "}\n"
        + "],\n"
        + "\"state\":\"DEPLOY\"\n"
        + "}\n"
        + "\n";

    String serviceList2String = "{\n"
        + "\"services\": \n"
        + "[\n"
        + "{\n"
        + "\"name\":\"flink\",\n"
        + "\"state\":\n"
        + "{\n"
        + "\"ExitCode\":\"0\",\n"
        + "\"Status\":\"running\"\n"
        + "}\n"
        + "},\n"
        + "{\n"
        + "\"name\":\"device-service\",\n"
        + "\"state\":\n"
        + "{\n"
        + "\"ExitCode\":\"0\",\n"
        + "\"Status\":\"exited\"\n"
        + "}\n"
        + "}\n"
        + "],\n"
        + "\"state\":\"DEPLOY\"\n"
        + "}\n"
        + "\n";

    String serviceList3String = "{\n"
        + "\"services\": \n"
        + "[\n"
        + "{\n"
        + "\"name\":\"aaa\",\n"
        + "\"state\":\n"
        + "{\n"
        + "\"ExitCode\":\"0\",\n"
        + "\"Status\":\"running\"\n"
        + "}\n"
        + "},\n"
        + "{\n"
        + "\"name\":\"bbb\",\n"
        + "\"state\":\n"
        + "{\n"
        + "\"ExitCode\":\"0\",\n"
        + "\"Status\":\"exited\"\n"
        + "}\n"
        + "}\n"
        + "],\n"
        + "\"state\":\"DEPLOY\"\n"
        + "}\n"
        + "\n";

    JsonParser parser = new JsonParser();
    when(mockedHttp.get(GROUP_API_BASE + "/test")).thenReturn(parser.parse(edgeListString));
    when(mockedHttp.get(AGENT_API_BASE + "/edge1")).thenReturn(parser.parse(edgeInfo1String));
    when(mockedHttp.get(AGENT_API_BASE + "/edge2")).thenReturn(parser.parse(edgeInfo2String));
    when(mockedHttp.get(AGENT_API_BASE + "/edge3")).thenReturn(parser.parse(edgeInfo3String));
    when(mockedHttp.get(AGENT_API_BASE + "/edge1" + APPS_URL + "/app1"))
        .thenReturn(parser.parse(serviceList1String));
    when(mockedHttp.get(AGENT_API_BASE + "/edge2" + APPS_URL + "/app2"))
        .thenReturn(parser.parse(serviceList2String));
    when(mockedHttp.get(AGENT_API_BASE + "/edge3" + APPS_URL + "/app3"))
        .thenReturn(parser.parse(serviceList3String));

    List<String> result1 = edgeInfo.getEngineList("test", "KAPACITOR");
    List<String> result2 = edgeInfo.getEngineList("test", "FLINK");
    List<String> result3 = edgeInfo.getEngineList("test", "STORM");

    Assert.assertTrue(result1.size() == 1);
    Assert.assertTrue(result2.size() == 1);
    Assert.assertTrue(result3.size() == 0);
  }
}
