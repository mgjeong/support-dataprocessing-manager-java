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
