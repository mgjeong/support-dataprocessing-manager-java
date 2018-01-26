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
public class PharosRestClientTest {

  private static PharosRestClient pharosRestClient;
  private static HTTP mockedHttp;

  @BeforeClass
  public static void initialize() throws Exception {
    mockedHttp = mock(HTTP.class);
    whenNew(HTTP.class).withNoArguments().thenReturn(mockedHttp);
    when(mockedHttp.initialize(PharosConstants.PHAROS_HOST, PharosConstants.PHAROS_PORT, "http"))
        .thenReturn(null);
    pharosRestClient = new PharosRestClient();
  }

  @Test
  public void getGroupListSuccessTest() throws Exception {
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

    List<String> result = pharosRestClient.getGroupList();

    Assert.assertTrue("id1111".equals(result.get(0)));
    Assert.assertTrue("id2222".equals(result.get(1)));
    Assert.assertTrue("id3333".equals(result.get(2)));
  }

  @Test
  public void getGroupListFailureTest() throws Exception {

    JsonParser parser = new JsonParser();
    when(mockedHttp.get(GROUP_API_BASE)).thenReturn(null);

    List<String> result = pharosRestClient.getGroupList();
    Assert.assertTrue(result.size() == 0);
  }


  @Test
  public void getEdgeListSuccessTest() throws Exception {
    String testJsonString = "{\n"
        + "\"id\":\"id1111\",\n"
        + "\"members\": [\"edge1\", \"edge2\", \"edge3\"]\n"
        + "}";
    JsonParser parser = new JsonParser();
    when(mockedHttp.get(GROUP_API_BASE + "/test")).thenReturn(parser.parse(testJsonString));

    List<String> result = pharosRestClient.getEdgeIdList("test");

    Assert.assertTrue("edge1".equals(result.get(0)));
    Assert.assertTrue("edge2".equals(result.get(1)));
    Assert.assertTrue("edge3".equals(result.get(2)));
  }

  @Test
  public void getEdgeListFailureTest() throws Exception {

    JsonParser parser = new JsonParser();
    when(mockedHttp.get(GROUP_API_BASE + "/test")).thenReturn(null);

    List<String> result = pharosRestClient.getEdgeIdList("test");
    Assert.assertTrue(result.size() == 0);
  }

  @Test
  public void getEdgeInfoSuccessTest() throws Exception {
    String testJsonString = "{\n"
        + "\"id\":\"edge1\",\n"
        + "\"host\":\"192.168.0.1\",\n"
        + "\"port\":8080,\n"
        + "\"apps\" : [\"app1\", \"app2\", \"app3\"]\n"
        + "}";
    JsonParser parser = new JsonParser();
    when(mockedHttp.get(AGENT_API_BASE + "/edge1")).thenReturn(parser.parse(testJsonString));

    Map<String, ?> map = pharosRestClient.getEdgeInfo("edge1");

    Assert.assertTrue(map.get("id").equals("edge1"));
    Assert.assertTrue(map.get("host").equals("192.168.0.1"));
    Assert.assertTrue(((List<String>) map.get("apps")).get(0).equals("app1"));
    Assert.assertTrue(((List<String>) map.get("apps")).get(1).equals("app2"));
    Assert.assertTrue(((List<String>) map.get("apps")).get(2).equals("app3"));
  }

  @Test
  public void getEdgeInfoFailureTest() throws Exception {
    JsonParser parser = new JsonParser();
    when(mockedHttp.get(AGENT_API_BASE + "/edge1")).thenReturn(null);

    Map<String, ?> map = pharosRestClient.getEdgeInfo("edge1");

    Assert.assertTrue(map == null);
  }

  @Test
  public void getServiceListSuccessTest() throws Exception {
    String testJsonString = "{\n"
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
    JsonParser parser = new JsonParser();
    when(mockedHttp.get(AGENT_API_BASE + "/test" + APPS_URL + "/test"))
        .thenReturn(parser.parse(testJsonString));

    List<String> result = pharosRestClient.getServiceList("test", "test");

    Assert.assertTrue(result.get(0).equals("kapacitor-engine"));
    Assert.assertTrue(result.size() == 1);
  }

  @Test
  public void getServiceListFailureTest() throws Exception {
    when(mockedHttp.get(AGENT_API_BASE + "/test" + APPS_URL + "/test")).thenReturn(null);

    List<String> result = pharosRestClient.getServiceList("test", "test");

    Assert.assertTrue(result.size() == 0);
  }
}
