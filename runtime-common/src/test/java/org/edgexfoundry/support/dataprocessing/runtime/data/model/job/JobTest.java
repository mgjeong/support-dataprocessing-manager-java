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

package org.edgexfoundry.support.dataprocessing.runtime.data.model.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

public class JobTest {

  @Test
  public void testSetterAndGetter() {
    Job job = Job.create("jobid", 1L);
    job.setConfigStr("{}");
    WorkflowData workflowData = new WorkflowData();
    job.setWorkflowData(workflowData);

    Assert.assertEquals("jobid", job.getId());
    Assert.assertEquals(1L, job.getWorkflowId().longValue());
    Assert.assertEquals("{}", job.getConfigStr());
    Assert.assertNotNull(job.getState());
    Assert.assertNotNull(job.getWorkflowData());

    Map<String, Object> config = new HashMap<>();
    config.put("sample", 1);
    job.setConfig(config);
    Assert.assertEquals(1, job.getConfig().size());
    Assert.assertEquals(1, (int) job.getConfig("sample"));
    job.addConfig("sampletwo", 2);
    Assert.assertEquals(2, job.getConfig().size());
  }

  @Test
  public void testInvalidSetter() {
    try {
      Job job = Job.create(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // success
    }

    Job job = Job.create("jobid", 1L);
    try {
      job.setConfig(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // success
    }

    try {
      job.setConfigStr(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // success
    }
  }

  @Test
  public void testObjectMapper() throws Exception {
    Job job = Job.create("jobid", 1L);

    job.addConfig("hello", "world");

    ObjectMapper objectMapper = Mockito.spy(new ObjectMapper());
    Mockito.when(objectMapper.writeValueAsString(Mockito.any()))
        .thenThrow(new JsonProcessingException("JsonProcessingException mocked!") {
        });
    Whitebox.setInternalState(job, "mapper", objectMapper);
    try {
      job.getConfigStr();
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // Success
    }

    try {
      job.setConfigStr("invalid");
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // Success
    }
  }

  @Test
  public void testCreate() {
    Job job = Job.create("jobId", 1L);
    Assert.assertEquals(1L, job.getWorkflowId().longValue());

    try {
      Job.create(null);
      Assert.fail("Should not reach here");
    } catch (Exception e) {
    }

    try {
      Job.create(null, 3L);
      Assert.fail("Should not reach here");
    } catch (Exception e) {
    }

    WorkflowData workflowData = new WorkflowData();
    workflowData.setWorkflowId(5L);
    job = Job.create(workflowData);
    Assert.assertEquals(5L, job.getWorkflowId(), 0);
  }

  @Test
  public void testEqualsTo() {
    Job jobA = new Job("jobId", 1L);
    Job jobB = new Job("jobId", 1L);
    Job jobC = new Job("jobC", 1L);

    Set<Job> jobs = new HashSet();
    jobs.add(jobA);
    jobs.add(jobB);
    jobs.add(jobC);

    Assert.assertEquals(2, jobs.size());
    Assert.assertTrue(jobA.equals(jobB));
    Assert.assertFalse(jobA.equals(jobC));
    Assert.assertFalse(jobA.equals("hello"));
  }
}
