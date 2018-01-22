package org.edgexfoundry.support.dataprocessing.runtime.data.model.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

public class JobTest {

  @Test
  public void testSetterAndGetter() {
    Job job = new Job();

    job.setId("jobid");
    job.setWorkflowId(1L);
    job.setConfigStr("{}");

    Assert.assertEquals("jobid", job.getId());
    Assert.assertEquals(1L, job.getWorkflowId().longValue());
    Assert.assertEquals("{}", job.getConfigStr());

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
    Job job = new Job();
    try {
      job.setWorkflowId(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // success
    }

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
    Job job = new Job();

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
    Job job = Job.create(1L);
    Assert.assertEquals(1L, job.getWorkflowId().longValue());
  }
}
