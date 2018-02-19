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
