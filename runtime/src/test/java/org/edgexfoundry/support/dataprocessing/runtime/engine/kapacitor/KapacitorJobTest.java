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
package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor;

import java.time.Instant;
import org.junit.Assert;
import org.junit.Test;

public class KapacitorJobTest {

  @Test
  public void testWithId() {
    KapacitorJob job = new KapacitorJob();
    String id = "test";
    job.setId(id);
    Assert.assertEquals(job.getId(), id);
  }

  @Test
  public void testWithStatus() {
    KapacitorJob job = new KapacitorJob();
    String status = "test status";
    job.setStatus(status);
    Assert.assertEquals(job.getStatus(), status);
  }

  @Test
  public void testWithExecuting() {
    KapacitorJob job = new KapacitorJob();
    boolean executing = false;
    job.setExecuting(executing);
    Assert.assertEquals(job.isExecuting(), executing);
  }

  @Test
  public void testWithError() {
    KapacitorJob job = new KapacitorJob();
    String error = "test error";
    job.setError(error);
    Assert.assertEquals(job.getError(), error);
  }

  @Test
  public void testWithCreated() {
    KapacitorJob job = new KapacitorJob();
    String created = Instant.now().toString();
    job.setCreated(created);
    Assert.assertEquals(job.getCreated(), created);
  }

  @Test
  public void testWithModified() {
    KapacitorJob job = new KapacitorJob();
    String modified = Instant.now().toString();
    job.setModified(modified);
    Assert.assertEquals(job.getModified(), modified);
  }

  @Test
  public void testWithLastEnabled() {
    KapacitorJob job = new KapacitorJob();
    String lastEnabled = Instant.now().toString();
    job.setLastEnabled(lastEnabled);
    Assert.assertEquals(job.getLastEnabled(), lastEnabled);
  }
}
