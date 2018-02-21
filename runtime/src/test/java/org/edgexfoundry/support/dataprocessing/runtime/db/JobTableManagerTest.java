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
package org.edgexfoundry.support.dataprocessing.runtime.db;

import java.util.Collection;
import java.util.UUID;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class JobTableManagerTest extends DatabaseTest {

  private static JobTableManager jobTable;

  @BeforeClass
  public static void setup() throws Exception {
    jobTable = JobTableManager.getInstance();
    java.lang.reflect.Field databaseField = AbstractStorageManager.class
        .getDeclaredField("database");
    databaseField.setAccessible(true);
    databaseField.set(jobTable,
        DatabaseManager.getInstance().getDatabase("jdbc:sqlite:" + testDB.getAbsolutePath()));

    ResourceLoader loader = new DefaultResourceLoader(ClassLoader.getSystemClassLoader());
    Resource resource = loader.getResource("db/sqlite/create_tables.sql");
    jobTable.executeSqlScript(resource);
  }

  @Test
  public void testGettingNonExistingJob() {
    // Invalid param
    try {
      jobTable.getJobById(null);
      Assert.fail("Should not reach here");
    } catch (Exception e) {
      // success
    }

    try {
      jobTable.getJobById("non-existing-job");
      Assert.fail("Should not reach here");
    } catch (Exception e) {
      Assert.assertTrue(e.getMessage().contains("not found"));
      // success
    }
  }

  @Test
  public void testGetJobs() {
    Job jobA = Job.create(UUID.randomUUID().toString(), 1L);
    Job jobB = Job.create(UUID.randomUUID().toString(), 2L);
    try {
      jobTable.addJob(jobA);
      jobTable.addJob(jobB);
      Collection<Job> jobs = jobTable.getJobs();
      Assert.assertTrue(!jobs.isEmpty());
    } finally {
      jobTable.removeJob(jobA.getId());
      jobTable.removeJob(jobB.getId());
    }
  }

  @Test
  public void testGetJobsByWorkflow() {
    Job jobA = Job.create(UUID.randomUUID().toString(), 1L);
    Job jobB = Job.create(UUID.randomUUID().toString(), 2L);
    try {
      jobTable.addJob(jobA);
      jobTable.addJob(jobB);
      Collection<Job> jobs = jobTable.getJobsByWorkflow(System.currentTimeMillis());
      Assert.assertTrue(jobs.isEmpty());
      jobs = jobTable.getJobsByWorkflow(1L);
      Assert.assertTrue(!jobs.isEmpty());
    } finally {
      jobTable.removeJob(jobA.getId());
      jobTable.removeJob(jobB.getId());
    }
  }

  @Test
  public void testUpdateJobState() {
    Job job = Job.create(UUID.randomUUID().toString(), 2L);
    try {
      jobTable.addJob(job);
      jobTable.updateJobState(job.getState());
    } finally {
      jobTable.removeJob(job.getId());
    }
  }

  @Test
  public void testAddInvalidJobState() {
    Job job = Job.create(UUID.randomUUID().toString(), 3L);
    try {
      jobTable.updateJobState(null);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }
  }

  @Test
  public void testAddJob() {
    Job job = Job.create(UUID.randomUUID().toString(), 4L);
    try {
      jobTable.addJob(job);
    } finally {
      jobTable.removeJob(job.getId());
    }
  }

  @Test
  public void testAddInvalidJob() {
    try {
      jobTable.addJob(null);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }
  }
}
