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

import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowDetailed.RunningStatus;
import org.junit.Assert;
import org.junit.Test;

public class WorkflowDetailedTest {

  @Test
  public void testSetterAndGetter() {
    WorkflowDetailed detailed = new WorkflowDetailed();
    Workflow workflow = new Workflow();
    detailed.setWorkflow(workflow);
    detailed.setRunning(RunningStatus.UNKNOWN);

    Assert.assertEquals(workflow, detailed.getWorkflow());
    Assert.assertEquals(RunningStatus.UNKNOWN, detailed.getRunning());
  }

  @Test
  public void testInvalidSetter() {
    WorkflowDetailed detailed = new WorkflowDetailed();
    try {
      detailed.setWorkflow(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // Success
    }

    try {
      detailed.setRunning(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // Success
    }
  }
}
