package org.edgexfoundry.support.dataprocessing.runtime.pharos;

import org.junit.Assert;
import org.junit.Test;

public class PharosConstantsTest {
  @Test
  public void variableTest() {
    Assert.assertTrue(PharosConstants.PHAROS_HOST.equals("10.113.66.234"));
    Assert.assertEquals(PharosConstants.PHAROS_PORT.longValue(), 48099);

    Assert.assertTrue(PharosConstants.PHAROS_JSON_SCHEMA_GROUPS.equals("groups"));
    Assert.assertTrue(PharosConstants.PHAROS_JSON_SCHEMA_GROUP_ID.equals("id"));
    Assert.assertTrue(PharosConstants.PHAROS_JSON_SCHEMA_GROUP_MEMBERS.equals("members"));
    Assert.assertTrue(PharosConstants.PHAROS_JSON_SCHEMA_AGENTS.equals("agents"));
    Assert.assertTrue(PharosConstants.PHAROS_JSON_SCHEMA_AGENT_ID.equals("id"));
    Assert.assertTrue(PharosConstants.PHAROS_JSON_SCHEMA_HOST_NAME.equals("host"));
    Assert.assertTrue(PharosConstants.PHAROS_JSON_SCHEMA_APPS.equals("apps"));
    Assert.assertTrue(PharosConstants.PHAROS_JSON_SCHEMA_SERVICES.equals("services"));
    Assert.assertTrue(PharosConstants.PHAROS_JSON_SCHEMA_APP_NAME.equals("name"));
    Assert.assertTrue(PharosConstants.PHAROS_JSON_SCHEMA_APP_STATE.equals("state"));
    Assert.assertTrue(PharosConstants.PHAROS_JSON_SCHEMA_APP_STATE_STATUS.equals("Status"));
    Assert.assertTrue(PharosConstants.PHAROS_JSON_SCHEMA_APP_STATE_RUNNING.equals("running"));

    Assert.assertTrue(PharosConstants.FLINK_NAME.equals("flink"));
    Assert.assertEquals(PharosConstants.FLINK_PORT.longValue(), 8081);

    Assert.assertTrue(PharosConstants.KAPACITOR_NAME.equals("kapacitor-engine"));
    Assert.assertEquals(PharosConstants.KAPACITOR_PORT.longValue(), 9092);
  }

}
