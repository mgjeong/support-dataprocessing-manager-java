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
package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

public class WorkflowOutputComponentTest {

  @Test
  public void testSetterAndGetter() {
    WorkflowOutputComponent component = new WorkflowSource();
    component.setOutputStreams(new ArrayList<>());
    Assert.assertEquals(0, component.getOutputStreams().size());

    WorkflowStream stream = new WorkflowStream();
    component.addOutputStream(stream);
    Assert.assertEquals(1, component.getOutputStreams().size());
  }

  @Test
  public void testInvalidSetter() {
    WorkflowOutputComponent component = new WorkflowSource();
    try {
      component.setOutputStreams(null);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }

    try {
      component.addOutputStream(null);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }
  }
}
